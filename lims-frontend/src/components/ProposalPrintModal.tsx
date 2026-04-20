import React, { useEffect, useRef } from 'react';
import { X, Printer, FileText } from 'lucide-react';
import styles from '../styles/ProposalPrintModal.module.css';
import { type CommercialProposal } from '../hooks/useProposals';

interface Props {
  isOpen: boolean;
  onClose: () => void;
  proposal: CommercialProposal | null;
}

export const ProposalPrintModal: React.FC<Props> = ({ isOpen, onClose, proposal }) => {
  const printRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleEsc = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose(); };
    if (isOpen) document.addEventListener('keydown', handleEsc);
    return () => document.removeEventListener('keydown', handleEsc);
  }, [isOpen, onClose]);

  const handlePrint = () => {
    window.print();
  };

  if (!isOpen || !proposal) return null;

  return (
    <div className={styles.overlay} onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}>
      <div className={styles.modal} role="dialog" aria-modal="true">
        <div className={styles.modalHeader}>
          <div className={styles.modalTitleGroup}>
            <div className={styles.modalIcon}><FileText size={22} /></div>
            <h2 className={styles.modalTitle}>Print Proposal</h2>
          </div>
          <button className={styles.closeBtn} onClick={onClose}><X size={20} /></button>
        </div>

        <div className={styles.modalContent}>
          <div className={styles.document} ref={printRef}>
            <div className={styles.docHeader}>
              <div>
                <h1 className={styles.docTitle}>Commercial Proposal</h1>
                <p style={{ margin: 0, color: '#64748B' }}>LIMS EcoLab Innovations</p>
              </div>
              <div style={{ textAlign: 'right' }}>
                <p style={{ margin: 0, fontSize: '1.25rem', fontWeight: 800, color: '#0F172A', fontFamily: 'var(--font-mono)' }}>
                  {proposal.proposalNumber}
                </p>
                <p style={{ margin: 0, color: '#64748B', fontSize: '0.875rem' }}>
                  Generated on {new Date(proposal.createdAt).toLocaleDateString()}
                </p>
              </div>
            </div>

            <div className={styles.docMeta}>
              <div className={styles.metaItem}>
                <strong>Customer / Client</strong>
                <span>{proposal.customerName}</span>
              </div>
              <div className={styles.metaItem}>
                <strong>Project Title</strong>
                <span>{proposal.title}</span>
              </div>
              <div className={styles.metaItem}>
                <strong>Valid Until</strong>
                <span>{proposal.validUntil ? new Date(proposal.validUntil).toLocaleDateString() : 'N/A'}</span>
              </div>
              <div className={styles.metaItem}>
                <strong>Account Manager</strong>
                <span>{proposal.createdBy}</span>
              </div>
            </div>

            <table className={styles.dataTable}>
              <thead>
                <tr>
                  <th>Item / Analysis</th>
                  <th>Description</th>
                  <th className={styles.rightText}>Qty</th>
                  <th className={styles.rightText}>Unit Price</th>
                  <th className={styles.rightText}>Total</th>
                </tr>
              </thead>
              <tbody>
                {proposal.items?.map((item, idx) => (
                  <tr key={idx}>
                    <td style={{ fontWeight: 600 }}>{item.analysisTypeCode || 'Custom'}</td>
                    <td>{item.description}</td>
                    <td className={styles.rightText}>{item.quantity}</td>
                    <td className={styles.rightText}>$ {item.unitPrice.toFixed(2)}</td>
                    <td className={styles.rightText}>$ {item.totalPrice?.toFixed(2) || (item.quantity * item.unitPrice).toFixed(2)}</td>
                  </tr>
                ))}
                {!proposal.items?.length && (
                  <tr>
                    <td colSpan={5} style={{ textAlign: 'center', color: '#94A3B8' }}>No items specified.</td>
                  </tr>
                )}
              </tbody>
            </table>

            <div className={styles.totals}>
              <div className={styles.totalRow}>
                <span>Subtotal:</span>
                <span>$ {proposal.totalAmount.toFixed(2)}</span>
              </div>
              {proposal.discount > 0 && (
                <div className={styles.totalRow}>
                  <span>Discount:</span>
                  <span style={{ color: '#EF4444' }}>- $ {proposal.discount.toFixed(2)}</span>
                </div>
              )}
              <div className={`${styles.totalRow} ${styles.final}`}>
                <span>Final Price:</span>
                <span>$ {proposal.finalAmount.toFixed(2)}</span>
              </div>
            </div>
            
            <div style={{ marginTop: '3rem', paddingTop: '1rem', borderTop: '1px dashed #CBD5E1', fontSize: '0.75rem', color: '#94A3B8', textAlign: 'center' }}>
              This proposal is generated automatically and serves as a valid agreement once accepted.<br/>
              Standard laboratory terms and conditions apply.
            </div>
          </div>
        </div>

        <div className={styles.modalFooter}>
          <button type="button" className={`${styles.printBtn} print-btn`} onClick={handlePrint}>
            <Printer size={18} /> Print Document
          </button>
        </div>
      </div>
    </div>
  );
};
