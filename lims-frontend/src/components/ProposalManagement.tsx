import React, { useState } from 'react';
import { useProposals, useApproveProposalMutation, useDeleteProposalMutation, type CommercialProposal } from '../hooks/useProposals';
import { ProposalRegistrationModal } from './ProposalRegistrationModal';
import { ProposalPrintModal } from './ProposalPrintModal';
import styles from '../styles/ProposalManagement.module.css';
import { Plus, Search, Loader2, AlertCircle, FileText, CheckCircle, Printer, Pencil, Trash2 } from 'lucide-react';

const statusBadgeMap: Record<string, string> = {
  DRAFT: styles.badgeDraft,
  SENT_TO_CUSTOMER: styles.badgeSent,
  APPROVED: styles.badgeApproved,
  DECLINED: styles.badgeDeclined,
};

const statusLabel: Record<string, string> = {
  DRAFT: 'Draft',
  SENT_TO_CUSTOMER: 'Sent',
  APPROVED: 'Approved',
  DECLINED: 'Declined',
};

export const ProposalManagement: React.FC = () => {
  const { data: proposals, isLoading, isError } = useProposals();
  const approveMutation = useApproveProposalMutation();
  const deleteMutation = useDeleteProposalMutation();

  const [searchTerm, setSearchTerm] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingProposal, setEditingProposal] = useState<CommercialProposal | null>(null);
  const [isPrintModalOpen, setIsPrintModalOpen] = useState(false);
  const [proposalToPrint, setProposalToPrint] = useState<CommercialProposal | null>(null);

  if (isLoading) {
    return (
      <main className={styles.container} style={{ justifyContent: 'center', alignItems: 'center' }}>
        <Loader2 size={40} style={{ color: 'var(--accent)', animation: 'spin 1s linear infinite' }} />
        <span style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>Loading commercial proposals...</span>
      </main>
    );
  }

  if (isError) {
    return (
      <main className={styles.container} style={{ justifyContent: 'center', alignItems: 'center' }}>
        <AlertCircle size={48} style={{ color: 'var(--semantic-error)' }} />
        <span style={{ marginTop: '1rem', color: 'var(--text-muted)' }}>Failed to load proposals.</span>
      </main>
    );
  }

  const handleApprove = async (id: number) => {
    if (window.confirm('By approving this proposal, it will be converted into a Service Order. Proceed?')) {
      try {
        await approveMutation.mutateAsync(id);
      } catch (err: any) {
        alert('Error approving: ' + (err.response?.data?.message || err.message));
      }
    }
  };

  const handleEdit = (p: CommercialProposal) => {
    setEditingProposal(p);
    setIsModalOpen(true);
  };

  const handleDelete = async (p: CommercialProposal) => {
    if (window.confirm(`Delete proposal "${p.proposalNumber}"? This action cannot be undone.`)) {
      try {
        await deleteMutation.mutateAsync(p.id);
      } catch (err: any) {
        alert('Error deleting: ' + (err.response?.data?.message || err.message));
      }
    }
  };

  const handlePrint = (p: CommercialProposal) => {
    setProposalToPrint(p);
    setIsPrintModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingProposal(null);
  };

  const filtered = proposals?.filter(p =>
    p.proposalNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
    p.customerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    p.title.toLowerCase().includes(searchTerm.toLowerCase())
  ) || [];

  const isEditable = (status: string) => status === 'DRAFT' || status === 'SENT_TO_CUSTOMER';

  return (
    <main className={styles.container}>
      <header className={styles.header}>
        <div className={styles.headerLeft}>
          <h1>Commercial CRM</h1>
          <p>Manage customer project proposals, quotes, and pricing.</p>
        </div>
        <button className={styles.newBtn} onClick={() => { setEditingProposal(null); setIsModalOpen(true); }}>
          <Plus size={18} /> New Proposal
        </button>
      </header>

      <section style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
        <div style={{ position: 'relative', flex: 1, maxWidth: '500px' }}>
          <Search size={18} style={{ position: 'absolute', left: '1rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
          <input
            type="text"
            placeholder="Search proposals by customer, number or title..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ width: '100%', padding: '0.875rem 1rem 0.875rem 2.8rem', borderRadius: '12px', border: '1px solid var(--border)', background: 'var(--surface)', color: 'var(--text)', fontFamily: 'inherit', boxSizing: 'border-box' }}
          />
        </div>
      </section>

      <ProposalRegistrationModal isOpen={isModalOpen} onClose={handleCloseModal} editData={editingProposal} />

      <ProposalPrintModal
        isOpen={isPrintModalOpen}
        onClose={() => setIsPrintModalOpen(false)}
        proposal={proposalToPrint}
      />

      <div className={styles.tableWrapper}>
        <table className={styles.dataTable}>
          <thead>
            <tr>
              <th>Proposal #</th>
              <th>Customer</th>
              <th>Project Title</th>
              <th>Total</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filtered.length === 0 ? (
              <tr>
                <td colSpan={6}>
                  <div className={styles.emptyState}>
                    <FileText size={48} className={styles.emptyIcon} />
                    <p>No proposals yet. Start by creating a new one!</p>
                  </div>
                </td>
              </tr>
            ) : (
              filtered.map(p => (
                <tr key={p.id}>
                  <td className={styles.fw600} style={{ fontFamily: 'var(--font-mono)', color: 'var(--accent)' }}>
                    {p.proposalNumber}
                  </td>
                  <td className={styles.fw500}>{p.customerName}</td>
                  <td>{p.title}</td>
                  <td className={styles.currency}>${p.finalAmount.toFixed(2)}</td>
                  <td>
                    <span className={`${styles.badge} ${statusBadgeMap[p.status] || ''}`}>
                      {statusLabel[p.status] || p.status}
                    </span>
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '0.4rem', alignItems: 'center', flexWrap: 'wrap' }}>
                      {/* View/Print */}
                      <button
                        className={styles.actionBtn}
                        style={{ background: 'var(--surface)', color: 'var(--text)', border: '1px solid var(--border)' }}
                        onClick={() => handlePrint(p)}
                        title="View & Print"
                      >
                        <Printer size={14} /> View
                      </button>

                      {/* Edit – only before approval */}
                      {isEditable(p.status) && (
                        <button
                          className={styles.actionBtn}
                          style={{ background: 'rgba(59,130,246,0.12)', color: '#60a5fa', border: '1px solid rgba(59,130,246,0.3)' }}
                          onClick={() => handleEdit(p)}
                          title="Edit proposal"
                        >
                          <Pencil size={14} /> Edit
                        </button>
                      )}

                      {/* Delete – only before approval */}
                      {isEditable(p.status) && (
                        <button
                          className={styles.actionBtn}
                          style={{ background: 'rgba(239,68,68,0.1)', color: '#f87171', border: '1px solid rgba(239,68,68,0.3)' }}
                          onClick={() => handleDelete(p)}
                          disabled={deleteMutation.isPending}
                          title="Delete proposal"
                        >
                          <Trash2 size={14} /> Delete
                        </button>
                      )}

                      {/* Approve */}
                      {p.status !== 'APPROVED' && p.status !== 'DECLINED' ? (
                        <button
                          className={styles.actionBtn}
                          onClick={() => handleApprove(p.id)}
                          disabled={approveMutation.isPending}
                          title="Approve and generate Service Order"
                        >
                          <CheckCircle size={14} /> Approve & SO
                        </button>
                      ) : p.status === 'APPROVED' ? (
                        <span style={{ fontSize: '0.75rem', color: 'var(--accent-secondary)', fontWeight: 600 }}>
                          ✔ OS #{p.serviceOrderId}
                        </span>
                      ) : null}
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </main>
  );
};
