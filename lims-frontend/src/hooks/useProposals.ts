import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../api/axiosClient';

export interface ProposalItem {
  description: string;
  quantity: number;
  unitPrice: number;
  totalPrice?: number;
  analysisTypeId?: number;
  analysisTypeCode?: string;
}

export interface CommercialProposal {
  id: number;
  proposalNumber: string;
  title: string;
  status: 'DRAFT' | 'SENT_TO_CUSTOMER' | 'APPROVED' | 'DECLINED';
  totalAmount: number;
  discount: number;
  finalAmount: number;
  createdAt: string;
  validUntil: string;
  customerName: string;
  customerId: number;
  createdBy: string;
  serviceOrderId?: number;
  legislationId?: number;
  legislationName?: string;
  items: ProposalItem[];
}

export interface ProposalRequest {
  customerId: number;
  legislationId?: number;
  title: string;
  validUntil?: string;
  items: ProposalItem[];
}

export const useProposals = () => {
  return useQuery<CommercialProposal[], Error>({
    queryKey: ['proposals'],
    queryFn: async () => {
      const response = await apiClient.get('/proposals');
      return response.data;
    },
    staleTime: 1000 * 60,
  });
};

export const useCreateProposalMutation = () => {
  const queryClient = useQueryClient();
  return useMutation<CommercialProposal, Error, ProposalRequest>({
    mutationFn: async (newProposal) => {
      const response = await apiClient.post('/proposals', newProposal);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['proposals'] });
    },
  });
};

export const useApproveProposalMutation = () => {
  const queryClient = useQueryClient();
  return useMutation<CommercialProposal, Error, number>({
    mutationFn: async (proposalId) => {
      const response = await apiClient.post(`/proposals/${proposalId}/approve`);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['proposals'] });
      queryClient.invalidateQueries({ queryKey: ['samples'] });
    },
  });
};

export const useUpdateProposalMutation = () => {
  const queryClient = useQueryClient();
  return useMutation<CommercialProposal, Error, { id: number; payload: ProposalRequest }>({
    mutationFn: async ({ id, payload }) => {
      const response = await apiClient.put(`/proposals/${id}`, payload);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['proposals'] });
    },
  });
};

export const useDeleteProposalMutation = () => {
  const queryClient = useQueryClient();
  return useMutation<void, Error, number>({
    mutationFn: async (id) => {
      await apiClient.delete(`/proposals/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['proposals'] });
    },
  });
};
