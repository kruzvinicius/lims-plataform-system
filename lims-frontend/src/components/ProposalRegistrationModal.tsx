import React, { useState, useEffect } from 'react';
import { X, User, Plus, Trash2, Loader2, CheckCircle, Pencil } from 'lucide-react';
import styles from '../styles/SampleRegistrationModal.module.css';
import appStyles from '../styles/ClinicalActionPanel.module.css';

import { useCustomers } from '../hooks/useCustomers';
import { useAnalysisTypes } from '../hooks/useAnalysisTypes';
import { useLegislations } from '../hooks/useLegislations';
import {
  useCreateProposalMutation,
  useUpdateProposalMutation,
  type ProposalItem,
  type CommercialProposal,
} from '../hooks/useProposals';

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  editData?: CommercialProposal | null;
}

export const ProposalRegistrationModal: React.FC<ModalProps> = ({ isOpen, onClose, editData }) => {
  const { data: customers } = useCustomers();
  const { data: analysisTypes } = useAnalysisTypes(true);
  const { data: legislations } = useLegislations();
  const createMutation = useCreateProposalMutation();
  const updateMutation = useUpdateProposalMutation();

  const isEditing = !!editData?.id;

  const [customerId, setCustomerId] = useState<number | ''>('');
  const [legislationId, setLegislationId] = useState<number | ''>('');
  const [title, setTitle] = useState('');
  const [items, setItems] = useState<ProposalItem[]>([]);

  // Item builder state
  const [itemType, setItemType] = useState<'ANALYSIS' | 'FEE'>('ANALYSIS');
  const [selectedAnalysisId, setSelectedAnalysisId] = useState<number | ''>('');
  const [feeDesc, setFeeDesc] = useState('');
  const [unitPrice, setUnitPrice] = useState<number | ''>('');
  const [qty, setQty] = useState<number | ''>(1);

  // Populate form when editing
  useEffect(() => {
    if (isOpen && editData) {
      setCustomerId(editData.customerId);
      setLegislationId(editData.legislationId || '');
      setTitle(editData.title);
      setItems(editData.items.map(i => ({
        description: i.description,
        quantity: i.quantity,
        unitPrice: i.unitPrice,
        analysisTypeId: i.analysisTypeId,
        analysisTypeCode: i.analysisTypeCode,
      })));
    } else if (isOpen && !editData) {
      setCustomerId('');
      setLegislationId('');
      setTitle('');
      setItems([]);
    }
  }, [isOpen, editData]);

  if (!isOpen) return null;

  const handleAddItem = () => {
    if (!unitPrice || !qty) return;

    if (itemType === 'ANALYSIS' && selectedAnalysisId !== '') {
      const at = analysisTypes?.find(a => a.id === Number(selectedAnalysisId));
      if (at) {
        setItems(prev => [...prev, {
          description: `Analysis: ${at.name} (${at.code})`,
          quantity: Number(qty),
          unitPrice: Number(unitPrice),
          analysisTypeId: at.id,
          analysisTypeCode: at.code,
        }]);
      }
    } else if (itemType === 'FEE' && feeDesc) {
      setItems(prev => [...prev, {
        description: feeDesc,
        quantity: Number(qty),
        unitPrice: Number(unitPrice),
      }]);
    }

    setSelectedAnalysisId('');
    setFeeDesc('');
    setUnitPrice('');
    setQty(1);
  };

  const handleRemoveItem = (index: number) => {
    setItems(items.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!customerId || !title || items.length === 0) return;

    const payload = { 
      customerId: Number(customerId), 
      legislationId: legislationId ? Number(legislationId) : undefined,
      title, 
      items 
    };

    try {
      if (isEditing && editData?.id) {
        await updateMutation.mutateAsync({ id: editData.id, payload });
      } else {
        await createMutation.mutateAsync(payload);
      }
      setCustomerId('');
      setTitle('');
      setItems([]);
      onClose();
    } catch (err) {
      console.error(err);
      alert(`Failed to ${isEditing ? 'update' : 'create'} proposal.`);
    }
  };

  const totalProposalValue = items.reduce((sum, item) => sum + (item.unitPrice * item.quantity), 0);
  const isPending = createMutation.isPending || updateMutation.isPending;

  return (
    <div className={styles.overlay} onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}>
      <div className={styles.modal} style={{ maxWidth: '800px' }}>
        <header className={styles.header}>
          <h2>{isEditing ? '✏️ Edit Proposal' : 'Create Commercial Proposal 🛒'}</h2>
          <button className={styles.closeBtn} onClick={onClose}><X size={20} /></button>
        </header>

        <form className={styles.content} onSubmit={handleSubmit} style={{ overflowY: 'auto', maxHeight: '70vh' }}>

          <div className={styles.formGroup}>
            <label><User size={16} /> Customer</label>
            <select
              value={customerId}
              onChange={e => setCustomerId(Number(e.target.value) || '')}
              required
              className={styles.input}
            >
              <option value="">-- Select Customer --</option>
              {customers?.map(c => (
                <option key={c.id} value={c.id}>{c.corporateReason} (CNPJ/CPF: {c.taxId})</option>
              ))}
            </select>
          </div>

          <div className={styles.formGroup}>
            <label>Environmental Legislation (Optional)</label>
            <select
              value={legislationId}
              onChange={e => setLegislationId(Number(e.target.value) || '')}
              className={styles.input}
            >
              <option value="">-- No legislation (defaults apply) --</option>
              {legislations?.filter(l => l.active).map(l => (
                <option key={l.id} value={l.id}>{l.code} - {l.name}</option>
              ))}
            </select>
          </div>

          <div className={styles.formGroup}>
            <label>Project Title / Reference</label>
            <input
              type="text"
              placeholder="e.g. Monthly Water Monitoring – Q3"
              value={title}
              onChange={e => setTitle(e.target.value)}
              required
              className={styles.input}
            />
          </div>

          <hr style={{ margin: '1rem 0', borderColor: '#E2E8F0' }} />

          <h3 style={{ margin: 0, fontSize: '1rem' }}>Proposal Items</h3>

          {/* Add item row */}
          <div style={{ display: 'grid', gridTemplateColumns: 'minmax(120px, 1.2fr) 2fr 0.8fr 1fr auto', gap: '0.5rem', alignItems: 'end', background: '#F8FAFC', padding: '1rem', borderRadius: '8px', border: '1px solid #E2E8F0', marginTop: '0.75rem' }}>

            <div className={styles.formGroup} style={{ margin: 0 }}>
              <label style={{ fontSize: '0.75rem' }}>Item Type</label>
              <select value={itemType} onChange={e => setItemType(e.target.value as any)} className={styles.input}>
                <option value="ANALYSIS">Analysis / Method</option>
                <option value="FEE">Fee (Travel, ART…)</option>
              </select>
            </div>

            <div className={styles.formGroup} style={{ margin: 0 }}>
              <label style={{ fontSize: '0.75rem' }}>Description</label>
              {itemType === 'ANALYSIS' ? (
                <select
                  value={selectedAnalysisId}
                  onChange={e => {
                    const id = Number(e.target.value);
                    setSelectedAnalysisId(id || '');
                    if (id) {
                      const at = analysisTypes?.find(a => a.id === id);
                      if (at && at.defaultPrice != null) setUnitPrice(at.defaultPrice);
                    }
                  }}
                  className={styles.input}
                >
                  <option value="">Select Parameter</option>
                  {analysisTypes?.map(at => (
                    <option key={at.id} value={at.id}>
                      {at.name} ({at.code}){at.defaultPrice != null ? ` – $${at.defaultPrice.toFixed(2)}` : ''}
                    </option>
                  ))}
                </select>
              ) : (
                <input type="text" placeholder="e.g. Technical Visit" value={feeDesc} onChange={e => setFeeDesc(e.target.value)} className={styles.input} />
              )}
            </div>

            <div className={styles.formGroup} style={{ margin: 0 }}>
              <label style={{ fontSize: '0.75rem' }}>Qty</label>
              <input type="number" min="1" value={qty} onChange={e => setQty(Number(e.target.value))} className={styles.input} />
            </div>

            <div className={styles.formGroup} style={{ margin: 0 }}>
              <label style={{ fontSize: '0.75rem' }}>Unit Price ($)</label>
              <input type="number" step="0.01" value={unitPrice} onChange={e => setUnitPrice(Number(e.target.value))} className={styles.input} />
            </div>

            <button
              type="button"
              onClick={handleAddItem}
              className={appStyles.addBtn}
              style={{ height: '42px', marginBottom: '2px' }}
              disabled={!qty || !unitPrice || (itemType === 'ANALYSIS' ? !selectedAnalysisId : !feeDesc)}
            >
              <Plus size={18} />
            </button>
          </div>

          {/* Cart table */}
          {items.length > 0 && (
            <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '1rem', fontSize: '0.875rem' }}>
              <thead>
                <tr style={{ borderBottom: '2px solid #E2E8F0', textAlign: 'left' }}>
                  <th style={{ padding: '0.5rem' }}>Description</th>
                  <th>Qty</th>
                  <th>Unit $</th>
                  <th>Total</th>
                  <th><span className="sr-only">Ações</span></th>
                </tr>
              </thead>
              <tbody>
                {items.map((it, idx) => (
                  <tr key={idx} style={{ borderBottom: '1px solid #E2E8F0' }}>
                    <td style={{ padding: '0.5rem' }}>{it.description}</td>
                    <td>{it.quantity}</td>
                    <td>R$ {it.unitPrice.toFixed(2)}</td>
                    <td style={{ fontWeight: 600 }}>R$ {(it.quantity * it.unitPrice).toFixed(2)}</td>
                    <td>
                      <button type="button" onClick={() => handleRemoveItem(idx)} style={{ color: '#EF4444', background: 'none', border: 'none', cursor: 'pointer' }}>
                        <Trash2 size={16} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}

        </form>

        <footer className={styles.footer} style={{ justifyContent: 'space-between', display: 'flex', alignItems: 'center' }}>
          <div>
            <span style={{ fontSize: '0.875rem', color: '#64748B' }}>Total Value: </span>
            <strong style={{ fontSize: '1.25rem', color: '#0F172A', fontFamily: 'monospace' }}>
              R$ {totalProposalValue.toFixed(2)}
            </strong>
          </div>
          <div style={{ display: 'flex', gap: '1rem' }}>
            <button type="button" className={styles.cancelBtn} onClick={onClose}>Cancel</button>
            <button
              type="submit"
              className={styles.submitBtn}
              onClick={handleSubmit}
              disabled={isPending || !customerId || !title || items.length === 0}
            >
              {isPending ? <Loader2 className={styles.spinner} size={18} /> : isEditing ? <Pencil size={18} /> : <CheckCircle size={18} />}
              {isEditing ? 'Save Changes' : 'Generate Proposal'}
            </button>
          </div>
        </footer>
      </div>
    </div>
  );
};
