package com.kruzvinicius.limsbackend.service;

import com.kruzvinicius.limsbackend.dto.*;
import com.kruzvinicius.limsbackend.dto.exception.EntityNotFoundException;
import com.kruzvinicius.limsbackend.model.*;
import com.kruzvinicius.limsbackend.model.enums.ProposalStatus;
import com.kruzvinicius.limsbackend.model.enums.SampleStatus;
import com.kruzvinicius.limsbackend.model.enums.ServiceOrderPriority;
import com.kruzvinicius.limsbackend.model.enums.TestResultStatus;
import com.kruzvinicius.limsbackend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProposalService {

    private final CommercialProposalRepository proposalRepository;
    private final ProposalItemRepository itemRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final AnalysisTypeRepository analysisTypeRepository;
    private final ServiceOrderService serviceOrderService;
    private final ServiceOrderRepository serviceOrderRepository;
    private final SampleRepository sampleRepository;
    private final TestResultRepository testResultRepository;
    private final LegislationRepository legislationRepository;

    @Transactional
    public ProposalResponseDTO createProposal(ProposalRequestDTO dto, String username) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
        User createdBy = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        EnvironmentalLegislation legislation = null;
        if (dto.getLegislationId() != null) {
            legislation = legislationRepository.findById(dto.getLegislationId())
                    .orElseThrow(() -> new EntityNotFoundException("Legislation not found"));
        }

        CommercialProposal proposal = CommercialProposal.builder()
                .proposalNumber(generateProposalNumber())
                .title(dto.getTitle())
                .validUntil(dto.getValidUntil())
                .customer(customer)
                .createdBy(createdBy)
                .legislation(legislation)
                .status(ProposalStatus.DRAFT)
                .totalAmount(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .finalAmount(BigDecimal.ZERO)
                .build();

        CommercialProposal saved = proposalRepository.save(proposal);

        if (dto.getItems() != null) {
            BigDecimal total = BigDecimal.ZERO;
            for (ProposalItemRequestDTO itemDto : dto.getItems()) {
                ProposalItem item = new ProposalItem();
                item.setProposal(saved);
                item.setDescription(itemDto.getDescription());
                item.setQuantity(itemDto.getQuantity());
                item.setUnitPrice(itemDto.getUnitPrice());
                
                BigDecimal itemTotal = itemDto.getUnitPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
                item.setTotalPrice(itemTotal);
                total = total.add(itemTotal);

                if (itemDto.getAnalysisTypeId() != null) {
                    AnalysisType at = analysisTypeRepository.findById(itemDto.getAnalysisTypeId())
                            .orElseThrow(() -> new EntityNotFoundException("AnalysisType not found"));
                    item.setAnalysisType(at);
                }

                itemRepository.save(item);
                saved.getItems().add(item);
            }
            saved.setTotalAmount(total);
            saved.setFinalAmount(total);
            saved = proposalRepository.save(saved);
        }

        return mapToDTO(saved);
    }

    @Transactional
    public ProposalResponseDTO approveProposal(Long id, String username) {
        CommercialProposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proposal not found"));

        if (proposal.getStatus() != ProposalStatus.DRAFT && proposal.getStatus() != ProposalStatus.SENT_TO_CUSTOMER) {
            throw new IllegalStateException("Proposal cannot be approved from status: " + proposal.getStatus());
        }

        proposal.setStatus(ProposalStatus.APPROVED);

        // --- AUTOMATIC SERVICE ORDER GENERATION ---
        log.info("Generating Operational Service Order from Commercial Proposal {}", proposal.getProposalNumber());

        // 1. Create a placeholder Sample for the field collection tracking
        Sample fieldSample = new Sample();
        fieldSample.setDescription("Field Collection / Batch for " + proposal.getTitle());
        fieldSample.setBarcode("SPL-" + System.currentTimeMillis());
        fieldSample.setCustomer(proposal.getCustomer());
        fieldSample.setNotes("Auto-generated from Proposal " + proposal.getProposalNumber());
        fieldSample.setStatus(SampleStatus.PENDING_RECEIPT);
        if (proposal.getLegislation() != null) {
            fieldSample.setLegislation(proposal.getLegislation());
        }
        Sample savedSample = sampleRepository.save(fieldSample);

        // 1.5 Generate Test Results from the Proposal Items
        for (ProposalItem item : proposal.getItems()) {
            if (item.getAnalysisType() != null) {
                TestResult tr = new TestResult();
                tr.setSample(savedSample);
                tr.setParameterName(item.getAnalysisType().getName());
                tr.setUnit(item.getAnalysisType().getDefaultUnit() != null ? item.getAnalysisType().getDefaultUnit() : "N/A");
                tr.setResultValue(""); // Result is empty pending analysis
                tr.setStatus(TestResultStatus.PENDING);
                testResultRepository.save(tr);
            }
        }

        // 2. Delegate ServiceOrder Creation to ServiceOrderService
        ServiceOrderDTO soParams = new ServiceOrderDTO(
                null, null, "Order derived from Proposal: " + proposal.getTitle(),
                null, ServiceOrderPriority.NORMAL, null,
                null, null, null, null,
                proposal.getCustomer().getId(),
                null, null, List.of(savedSample.getId())
        );

        ServiceOrderDTO createdSO = serviceOrderService.create(soParams, username);

        // 3. Link them back
        ServiceOrder soEntity = serviceOrderRepository.findById(createdSO.id())
                .orElseThrow(() -> new EntityNotFoundException("Created ServiceOrder not found"));
        proposal.setServiceOrder(soEntity);
        proposal = proposalRepository.save(proposal);
        
        ProposalResponseDTO response = mapToDTO(proposal);
        response.setServiceOrderId(createdSO.id()); // Just pass it in the response so UI knows
        
        return response;
    }

    @Transactional(readOnly = true)
    public List<ProposalResponseDTO> findAll() {
        return proposalRepository.findAllByOrderByCreatedAtDesc().stream().map(this::mapToDTO).toList();
    }

    @Transactional
    public ProposalResponseDTO updateProposal(Long id, ProposalRequestDTO dto, String username) {
        CommercialProposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proposal not found: " + id));

        if (proposal.getStatus() == ProposalStatus.APPROVED) {
            throw new IllegalStateException("Cannot edit a proposal that has already been approved (formalized).");
        }

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        EnvironmentalLegislation legislation = null;
        if (dto.getLegislationId() != null) {
            legislation = legislationRepository.findById(dto.getLegislationId())
                    .orElseThrow(() -> new EntityNotFoundException("Legislation not found"));
        }

        proposal.setTitle(dto.getTitle());
        proposal.setValidUntil(dto.getValidUntil());
        proposal.setCustomer(customer);
        proposal.setLegislation(legislation);

        // Replace items
        proposal.getItems().clear();
        proposalRepository.save(proposal); // flush orphans

        if (dto.getItems() != null) {
            BigDecimal total = BigDecimal.ZERO;
            for (ProposalItemRequestDTO itemDto : dto.getItems()) {
                ProposalItem item = new ProposalItem();
                item.setProposal(proposal);
                item.setDescription(itemDto.getDescription());
                item.setQuantity(itemDto.getQuantity());
                item.setUnitPrice(itemDto.getUnitPrice());
                BigDecimal itemTotal = itemDto.getUnitPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
                item.setTotalPrice(itemTotal);
                total = total.add(itemTotal);

                if (itemDto.getAnalysisTypeId() != null) {
                    AnalysisType at = analysisTypeRepository.findById(itemDto.getAnalysisTypeId())
                            .orElseThrow(() -> new EntityNotFoundException("AnalysisType not found"));
                    item.setAnalysisType(at);
                }
                itemRepository.save(item);
                proposal.getItems().add(item);
            }
            proposal.setTotalAmount(total);
            proposal.setFinalAmount(total);
        }

        log.info("Proposal {} updated by {}", id, username);
        return mapToDTO(proposalRepository.save(proposal));
    }

    @Transactional
    public void deleteProposal(Long id) {
        CommercialProposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proposal not found: " + id));

        if (proposal.getStatus() == ProposalStatus.APPROVED) {
            throw new IllegalStateException("Cannot delete a proposal that has already been approved (formalized).");
        }

        proposalRepository.delete(proposal);
        log.info("Proposal {} deleted.", id);
    }

    private ProposalResponseDTO mapToDTO(CommercialProposal p) {

        ProposalResponseDTO dto = new ProposalResponseDTO();
        dto.setId(p.getId());
        dto.setProposalNumber(p.getProposalNumber());
        dto.setTitle(p.getTitle());
        dto.setStatus(p.getStatus().name());
        dto.setTotalAmount(p.getTotalAmount());
        dto.setDiscount(p.getDiscount());
        dto.setFinalAmount(p.getFinalAmount());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setValidUntil(p.getValidUntil());
        dto.setCustomerName(p.getCustomer() != null ? p.getCustomer().getCorporateReason() : null);
        dto.setCustomerId(p.getCustomer() != null ? p.getCustomer().getId() : null);
        dto.setCreatedBy(p.getCreatedBy() != null ? p.getCreatedBy().getUsername() : null);
        dto.setServiceOrderId(p.getServiceOrder() != null ? p.getServiceOrder().getId() : null);
        dto.setLegislationId(p.getLegislation() != null ? p.getLegislation().getId() : null);
        dto.setLegislationName(p.getLegislation() != null ? (p.getLegislation().getCode() + " - " + p.getLegislation().getName()) : null);

        if (p.getItems() != null) {
            dto.setItems(p.getItems().stream().map(i -> {
                ProposalItemResponseDTO iDto = new ProposalItemResponseDTO();
                iDto.setId(i.getId());
                iDto.setDescription(i.getDescription());
                iDto.setQuantity(i.getQuantity());
                iDto.setUnitPrice(i.getUnitPrice());
                iDto.setTotalPrice(i.getTotalPrice());
                if (i.getAnalysisType() != null) {
                    iDto.setAnalysisTypeId(i.getAnalysisType().getId());
                    iDto.setAnalysisTypeCode(i.getAnalysisType().getCode());
                }
                return iDto;
            }).toList());
        }

        return dto;
    }

    private String generateProposalNumber() {
        return String.format("PROP-%d-%04d", Year.now().getValue(), (int)(Math.random() * 10000));
    }
}
