import React, { useEffect, useRef } from 'react';
import { X, Printer, Award } from 'lucide-react';
import styles from '../styles/CertificatePrintModal.module.css';
import { type Sample } from '../hooks/useSamples';
import { type TestResultDTO } from '../hooks/useClinicalMutations';
import { type AnalysisType } from '../hooks/useAnalysisTypes';
import { type Legislation } from '../hooks/useLegislations';

interface Props {
  isOpen: boolean;
  onClose: () => void;
  sample: Sample | null;
  results: TestResultDTO[] | undefined;
  analysisTypes: AnalysisType[] | undefined;
  /** The applicable environmental legislation for this sample (used to display VMP). */
  legislation?: Legislation | null;
}

export const CertificatePrintModal: React.FC<Props> = ({ isOpen, onClose, sample, results, analysisTypes, legislation }) => {
  const printRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleEsc = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose(); };
    if (isOpen) document.addEventListener('keydown', handleEsc);
    return () => document.removeEventListener('keydown', handleEsc);
  }, [isOpen, onClose]);

  const handlePrint = () => {
    window.print();
  };

  if (!isOpen || !sample) return null;

  return (
    <div className={styles.overlay} onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}>
      <div className={styles.modal} role="dialog" aria-modal="true">
        
        <div className={styles.modalHeader}>
          <div className={styles.modalTitleGroup}>
            <div className={styles.modalIcon}><Award size={22} /></div>
            <h2 className={styles.modalTitle}>Certificate of Analysis</h2>
          </div>
          <button className={styles.closeBtn} onClick={onClose}><X size={20} /></button>
        </div>

        <div className={styles.modalContent}>
          <div className={styles.document} ref={printRef}>
            
            <div className={styles.docHeader}>
              <div>
                <h1 className={styles.docTitle}>Certificate of Analysis</h1>
                <p style={{ margin: 0, color: '#64748B', fontWeight: 600 }}>LIMS EcoLab Innovations</p>
                <p style={{ margin: 0, color: '#94A3B8', fontSize: '0.85rem' }}>ISO/IEC 17025 Accredited Laboratory</p>
              </div>
              <div style={{ textAlign: 'right' }}>
                <p style={{ margin: 0, fontSize: '1.25rem', fontWeight: 800, color: '#0F172A', fontFamily: 'var(--font-mono)' }}>
                  {sample.barcode}
                </p>
                <p style={{ margin: 0, color: '#64748B', fontSize: '0.875rem' }}>
                  Issue Date: {new Date().toLocaleDateString()}
                </p>
              </div>
            </div>

            <div className={styles.docMeta}>
              <div className={styles.metaItem}>
                <strong>Customer / Client</strong>
                <span>{sample.patientName}</span>
              </div>
              <div className={styles.metaItem}>
                <strong>Sample Origin / Type</strong>
                <span>{sample.testType}</span>
              </div>
              <div className={styles.metaItem}>
                <strong>Collection Date</strong>
                <span>{new Date(sample.collectionDate).toLocaleDateString()}</span>
              </div>
              <div className={styles.metaItem}>
                <strong>LIMS Status</strong>
                <span>{sample.status.replace('_', ' ')}</span>
              </div>
              <div className={styles.metaItem}>
                <strong>Compliance Reference</strong>
                <span>{legislation ? `${legislation.code} - ${legislation.name}` : 'Not Specified'}</span>
              </div>
            </div>

            <h3 style={{ borderBottom: '1px solid #E2E8F0', paddingBottom: '0.5rem', marginBottom: '1rem', color: '#0F172A' }}>
              Analytical Results
            </h3>

            <table className={styles.dataTable}>
              <thead>
                <tr>
                  <th>Parameter / Analysis</th>
                  <th>Result</th>
                  <th>Unit</th>
                  <th>VMP (Legislation)</th>
                  <th>Analysis Date</th>
                </tr>
              </thead>
              <tbody>
                {results?.map((res, idx) => {
                  const at = analysisTypes?.find(t => t.name === res.parameterName);
                  // VMP comes from the legislation linked to the sample
                  const legParam = legislation?.parameters?.find(
                    p => p.analysisTypeId === at?.id || p.analysisTypeName === res.parameterName
                  );
                  const vmpMin = legParam?.vmpMin ?? null;
                  const vmpMax = legParam?.vmpMax ?? null;
                  const vmpUnit = legParam?.unit || at?.defaultUnit || '';

                  let isOOS = false;
                  let vmpString: string;
                  if (!legislation) {
                    vmpString = 'No legislation';
                  } else if (vmpMax == null) {
                    vmpString = 'N/A';
                  } else if (vmpMin != null) {
                    vmpString = `${vmpMin} – ${vmpMax} ${vmpUnit}`.trim();
                  } else {
                    vmpString = `≤ ${vmpMax} ${vmpUnit}`.trim();
                  }

                  if (vmpMax != null || vmpMin != null) {
                    const numVal = parseFloat(res.resultValue);
                    if (!isNaN(numVal)) {
                      if (vmpMax != null && numVal > vmpMax) isOOS = true;
                      if (vmpMin != null && numVal < vmpMin) isOOS = true;
                    }
                  }

                  return (
                    <tr key={idx}>
                      <td style={{ fontWeight: 600 }}>{res.parameterName}</td>
                      <td>
                        <span style={isOOS ? { color: '#B91C1C', fontWeight: 800 } : {}}>
                          {res.resultValue || 'ND'}
                        </span>
                        {isOOS && <span className={styles.badgeOOS}>Out of Specs</span>}
                      </td>
                      <td>{res.unit}</td>
                      <td style={{ fontWeight: (vmpMax != null || vmpMin != null) ? 600 : 400, color: (vmpMax != null || vmpMin != null) ? '#0F172A' : '#94A3B8' }}>
                        {vmpString}
                        {legislation && vmpMax == null && <span style={{ fontSize: '0.75rem', color: '#94A3B8' }}> (not listed)</span>}
                      </td>
                      <td>{res.performedAt ? new Date(res.performedAt).toLocaleDateString() : 'N/A'}</td>
                    </tr>
                  );
                })}
                {!results?.length && (
                  <tr>
                    <td colSpan={5} style={{ textAlign: 'center', color: '#94A3B8' }}>No analytical results recorded.</td>
                  </tr>
                )}
              </tbody>
            </table>

            <div className={styles.signatureSection}>
              <div className={styles.signatureBlock}>
                <div className={styles.signatureLine}></div>
                <div className={styles.signatureName}>Technical Analyst</div>
                <div className={styles.signatureRole}>Laboratory Operations</div>
              </div>
              <div className={styles.signatureBlock}>
                <div className={styles.signatureLine}></div>
                <div className={styles.signatureName}>Technical Manager</div>
                <div className={styles.signatureRole}>Quality Assurance CRQ-IV</div>
              </div>
            </div>

            <div style={{ marginTop: '3rem', paddingTop: '1rem', borderTop: '1px dashed #CBD5E1', fontSize: '0.75rem', color: '#94A3B8', textAlign: 'center' }}>
              The results herein apply only to the sample tested. This Certificate of Analysis may not be reproduced, 
              except in full, without written approval of the laboratory. <br/>
              <b>Note:</b> Parameters marked as "Out of Specs" failed to meet the specified range.
            </div>
          </div>
        </div>

        <div className={styles.modalFooter}>
          <button type="button" className={`${styles.printBtn} print-btn`} onClick={handlePrint}>
            <Printer size={18} /> Print Certificate
          </button>
        </div>

      </div>
    </div>
  );
};
