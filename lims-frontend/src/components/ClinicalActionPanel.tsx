import React, { useState } from 'react';
import { Activity, X, Plus, Beaker, CheckCircle, XCircle, Printer } from 'lucide-react';
import styles from '../styles/ClinicalActionPanel.module.css';
import { useSampleResults, useAddResultMutation, useUpdateResultMutation, useApproveSampleMutation, useRejectSampleMutation, useUpdateStatusMutation } from '../hooks/useClinicalMutations';
import { type Sample } from '../hooks/useSamples';
import { useAnalysisTypes } from '../hooks/useAnalysisTypes';
import { useLegislations } from '../hooks/useLegislations';
import { CertificatePrintModal } from './CertificatePrintModal';

interface ClinicalActionPanelProps {
  sample: Sample | null;
  onClose: () => void;
}

export const ClinicalActionPanel: React.FC<ClinicalActionPanelProps> = ({ sample, onClose }) => {
  const { data: results, isLoading } = useSampleResults(sample?.id || null);
  const addResultMutation = useAddResultMutation(sample?.id || null);
  const updateResultMutation = useUpdateResultMutation(sample?.id || null);
  const approveMutation = useApproveSampleMutation(sample?.id || null);
  const rejectMutation = useRejectSampleMutation(sample?.id || null);
  const updateStatusMutation = useUpdateStatusMutation(sample?.id || null);
  const { data: analysisTypes } = useAnalysisTypes(true);
  const { data: legislations } = useLegislations();

  const sampleLegislation = legislations?.find(l => l.id === sample?.legislationId);

  // Local state for the rapid entry form
  const [selectedParamCode, setSelectedParamCode] = useState('');
  const [resultVal, setResultVal] = useState('');
  const [isCertOpen, setIsCertOpen] = useState(false);
  const [pendingEdits, setPendingEdits] = useState<Record<number, string>>({});

  const selectedAnalysisType = analysisTypes?.find(at => at.code === selectedParamCode);

  if (!sample) return null;

  const isCompleted = sample.status === 'APPROVED' || sample.status === 'RELEASED' || sample.status === 'REJECTED';

  const handleAddResult = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedAnalysisType || !resultVal) return;

    await addResultMutation.mutateAsync({
      parameterName: selectedAnalysisType.name,
      resultValue: resultVal,
      unit: selectedAnalysisType.defaultUnit || '',
      status: 'PENDING_APPROVAL'
    });

    // Reset local state after submission
    setSelectedParamCode('');
    setResultVal('');
  };

  const handleUpdatePendingResult = async (id: number) => {
    const val = pendingEdits[id];
    if (val === undefined || val === '') return;
    try {
      await updateResultMutation.mutateAsync({ id, value: val });
    } catch (e) {
      console.error(e);
      alert('Failed to update value.');
    }
  };

  const handleApprove = async () => {
    if (window.confirm('Are you sure you want to digitally sign and approve these results?')) {
      // Due to LIMS workflow constraint: PENDING_RECEIPT -> RECEIVED -> IN_ANALYSIS -> PENDING_APPROVAL -> APPROVED
      try {
        let currentStatus = sample?.status;
        
        if (currentStatus === 'PENDING_RECEIPT') {
          await updateStatusMutation.mutateAsync('RECEIVED');
          currentStatus = 'RECEIVED';
        }
        if (currentStatus === 'RECEIVED') {
          await updateStatusMutation.mutateAsync('IN_ANALYSIS');
          currentStatus = 'IN_ANALYSIS';
        }
        if (currentStatus === 'IN_ANALYSIS') {
            await updateStatusMutation.mutateAsync('PENDING_APPROVAL');
        }
        
        // Finally run the official approval signature endpoint
        await approveMutation.mutateAsync({ reviewerUsername: 'admin' });
        onClose();
      } catch (err) {
        console.error('Failed the workflow ladder:', err);
        alert('Workflow error: Ensure sample is in a verifiable state.');
      }
    }
  };

  const handleReject = async () => {
    const reason = window.prompt('Please provide a mandatory reason for clinical rejection:');
    if (reason) {
      await rejectMutation.mutateAsync({ reviewerUsername: 'admin', reason });
      onClose();
    }
  };

  return (
    <div className={styles.overlay} onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}>
      <aside className={styles.panel}>
        
        {/* Header */}
        <header className={styles.header}>
          <div className={styles.titleArea}>
            <p className={styles.barcode}>{sample.barcode}</p>
            <p className={styles.subtitle}>{sample.testType} • {sample.patientName}</p>
          </div>
          <button className={styles.closeBtn} onClick={onClose}>
            <X size={20} />
          </button>
        </header>

        {/* Content Area */}
        <div className={styles.content}>
          
          {/* Data Entry Section (hidden if already approved/rejected) */}
          {!isCompleted && (
            <section>
              <h3 className={styles.sectionTitle}>
                <Beaker size={16} /> Data Entry
              </h3>
              <form className={styles.formRow} onSubmit={handleAddResult}>
                <select
                  className={styles.input}
                  value={selectedParamCode}
                  onChange={e => setSelectedParamCode(e.target.value)}
                  disabled={addResultMutation.isPending}
                  style={{ flex: 1.5 }}
                >
                  <option value="">Select Parameter...</option>
                  {analysisTypes?.map(at => (
                    <option key={at.id} value={at.code}>{at.name}</option>
                  ))}
                </select>
                <input
                  className={styles.input}
                  placeholder="Value"
                  type="number"
                  step="any"
                  value={resultVal}
                  onChange={e => setResultVal(e.target.value)}
                  disabled={addResultMutation.isPending || !selectedParamCode}
                  style={{ flex: 1 }}
                />
                <button 
                  type="submit" 
                  className={styles.addBtn}
                  disabled={!selectedParamCode || !resultVal || addResultMutation.isPending}
                >
                  <Plus size={18} />
                </button>
              </form>
            </section>
          )}

          {/* Results History */}
          <section>
            <h3 className={styles.sectionTitle}>
              <Activity size={16} /> Biochemical Results
            </h3>
            
            {isLoading ? (
              <p className={styles.noResults}>Loading laboratory results...</p>
            ) : results && results.length > 0 ? (
              results.map((res, i) => {
                // Find matching analysis type to evaluate limits
                const at = analysisTypes?.find(t => t.name === res.parameterName);
                const legParam = sampleLegislation?.parameters?.find(p => p.analysisTypeId === at?.id || p.analysisTypeName === res.parameterName);
                
                const vmpMin = legParam?.vmpMin ?? null;
                const vmpMax = legParam?.vmpMax ?? null;
                const numVal = parseFloat(res.resultValue);
                
                let isOOS = false;
                if (!isNaN(numVal)) {
                  if (vmpMin != null && numVal < vmpMin) isOOS = true;
                  if (vmpMax != null && numVal > vmpMax) isOOS = true;
                }

                return (
                  <div key={i} className={`${styles.resultCard} ${isOOS ? styles.resultCardOOS : ''}`}>
                    <div className={styles.resultParam}>{res.parameterName}</div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      {res.status === 'PENDING' ? (
                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                          <input 
                            className={styles.input} 
                            style={{ width: '80px', padding: '0.2rem' }} 
                            placeholder="Value" 
                            value={pendingEdits[res.id!] !== undefined ? pendingEdits[res.id!] : res.resultValue}
                            onChange={(e) => setPendingEdits(prev => ({ ...prev, [res.id!]: e.target.value }))}
                          />
                          <button 
                            type="button" 
                            className={styles.addBtn} 
                            style={{ height: '32px', padding: '0 0.5rem', borderRadius: '4px' }}
                            onClick={() => handleUpdatePendingResult(res.id!)}
                            disabled={updateResultMutation.isPending || pendingEdits[res.id!] === undefined}
                          >
                            Save
                          </button>
                        </div>
                      ) : (
                        <span className={styles.resultValue}>{res.resultValue}</span>
                      )}
                      <span className={styles.resultUnit}>{res.unit}</span>
                      {isOOS && (
                        <span className={styles.oosBadge} title={`Expected: ${vmpMin ?? '0'} - ${vmpMax ?? '∞'}`}>
                          OOS
                        </span>
                      )}
                    </div>
                  </div>
                );
              })
            ) : (
              <div className={styles.noResults}>
                No results injected into the machine yet.
              </div>
            )}
          </section>
        </div>

        {/* Signature Footer */}
        {!isCompleted && (() => {
          const hasOOS = results?.some(res => {
            const at = analysisTypes?.find(t => t.name === res.parameterName);
            const legParam = sampleLegislation?.parameters?.find(p => p.analysisTypeId === at?.id || p.analysisTypeName === res.parameterName);
            if (!legParam) return false;
            
            const numVal = parseFloat(res.resultValue);
            if (isNaN(numVal)) return false;
            
            if (legParam.vmpMin != null && numVal < legParam.vmpMin) return true;
            if (legParam.vmpMax != null && numVal > legParam.vmpMax) return true;
            return false;
          });

          return (
            <footer className={styles.footer}>
              <button 
                className={`${styles.actionBtn} ${styles.rejectBtn}`}
                onClick={handleReject}
                disabled={rejectMutation.isPending || approveMutation.isPending}
              >
                <XCircle size={18} /> Reject Sample
              </button>
              <button 
                className={`${styles.actionBtn} ${styles.approveBtn}`}
                onClick={handleApprove}
                disabled={!results?.length || approveMutation.isPending || rejectMutation.isPending}
                title={hasOOS ? "Sign and Release (Contains Out Of Specification results)" : !results?.length ? "Cannot approve without results" : "Sign and Release"}
              >
                <CheckCircle size={18} /> Approve & Release
              </button>
            </footer>
          );
        })()}

        {isCompleted && (
          <footer className={styles.footer} style={{ justifyContent: 'center' }}>
            <button 
              className={`${styles.actionBtn}`} 
              onClick={() => setIsCertOpen(true)}
              style={{ background: 'var(--surface-elevated)', color: 'var(--text)', border: '1px solid var(--border)', width: '100%', justifyContent: 'center' }}
            >
              <Printer size={18} /> View Certificate of Analysis
            </button>
          </footer>
        )}
      </aside>

      {/* Render Certificate Modal outside the aside but inside the overlay so it layers correctly */}
      <CertificatePrintModal
        isOpen={isCertOpen}
        onClose={() => setIsCertOpen(false)}
        sample={sample}
        results={results}
        analysisTypes={analysisTypes}
        legislation={sampleLegislation}
      />
    </div>
  );
};
