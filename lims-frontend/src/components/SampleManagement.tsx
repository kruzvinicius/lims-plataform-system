import React, { useState } from 'react';
import { useSamples, type SampleStatus } from '../hooks/useSamples';
import styles from '../styles/SampleManagement.module.css';
import { Search, Loader2, AlertCircle, TestTube, ArrowUpRight, Plus } from 'lucide-react';
import { SampleRegistrationModal } from './SampleRegistrationModal';
import { ClinicalActionPanel } from './ClinicalActionPanel';
import { type Sample } from '../hooks/useSamples';

export const SampleManagement: React.FC = () => {
  const { data: samples, isLoading, isError } = useSamples();
  const [searchTerm, setSearchTerm] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedSample, setSelectedSample] = useState<Sample | null>(null);

  // Fallback views for loading and error states
  if (isLoading) {
    return (
      <div className={styles.loadingContainer}>
        <Loader2 className={styles.spinner} size={40} />
        <span>Synchronizing samples with laboratory server...</span>
      </div>
    );
  }

  if (isError) {
    return (
      <div className={styles.alertContainer}>
        <AlertCircle className={styles.errorIcon} size={48} />
        <span>Communication failure with LIMS Backend. Operation suspended.</span>
      </div>
    );
  }

  // Frontend pipelined quick filter (in-memory for zero latency)
  const filteredSamples = samples?.filter(sample => 
    sample.barcode.includes(searchTerm) || 
    sample.patientName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    sample.testType.toLowerCase().includes(searchTerm.toLowerCase())
  ) || [];

  const formatStatus = (status: SampleStatus) => status.replace('_', ' ');

  return (
    <main className={styles.bentoContainer}>
      
      {/* Productivity Dashboard Bento Grid Header */}
      <header className={styles.bentoHeader}>
        <div className={styles.headerLeft}>
          <h1 className={styles.title}>Sample Management</h1>
          <p className={styles.subtitle}>Global systemic overview and real-time SLA tracking.</p>
        </div>
        
        <div className={styles.kpiWidget}>
          <div className={styles.kpiValue}>
            {samples?.filter(s => s.priority === 'URGENT').length || 0}
          </div>
          <div className={styles.kpiLabel}>Pending Critical Urgencies</div>
          <div className={`${styles.kpiIndicator} ${styles.warning}`}>
            <AlertCircle size={14} /> Active Priority Queue
          </div>
        </div>
      </header>

      {/* Global Toolbar and Free Search */}
      <section className={styles.toolbar}>
        <div className={styles.searchBox}>
          <Search size={18} className={styles.searchIcon} />
          <input 
            type="text" 
            placeholder="Search by barcode, patient, or clinical type..." 
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className={styles.searchInput}
          />
        </div>
        <button
          id="register-sample-btn"
          className={styles.registerBtn}
          onClick={() => setIsModalOpen(true)}
        >
          <Plus size={18} /> Register Sample
        </button>
      </section>

      <SampleRegistrationModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} />
      <ClinicalActionPanel sample={selectedSample} onClose={() => setSelectedSample(null)} />

      {/* Dense Data Table (Zebra sticky grid) */}
      <div className={styles.tableWrapper}>
        <table className={styles.dataTable}>
          <thead>
            <tr>
              <th><TestTube size={16} /> LIMS Code</th>
              <th>Patient Name</th>
              <th>Analysis Type</th>
              <th>Entry Date</th>
              <th>Priority/SLA</th>
              <th>Workflow Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredSamples.map((sample) => (
              <tr key={sample.id}>
                <td className={styles.barcodeColumn}>{sample.barcode}</td>
                <td className={styles.fw500}>{sample.patientName}</td>
                <td>{sample.testType}</td>
                <td>{new Date(sample.collectionDate).toLocaleDateString()}</td>
                <td>
                  <span className={`${styles.badge} ${styles['priority_' + sample.priority]}`}>
                    {sample.priority === 'URGENT' ? '🚨 Urgent' : 'Normal'}
                  </span>
                </td>
                <td>
                  <span className={`${styles.statusBadge} ${styles['status_' + sample.status.replace(/\s+/g, '')]}`}>
                    {formatStatus(sample.status)}
                  </span>
                </td>
                <td>
                  <button 
                    className={styles.actionBtn}
                    onClick={() => setSelectedSample(sample)}
                  >
                    View Details <ArrowUpRight size={14} />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </main>
  );
};
