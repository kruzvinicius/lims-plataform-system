import React, { useState } from 'react';
import { useNonConformances } from '../hooks/useNonConformances';
import styles from '../styles/NonConformanceManagement.module.css';
import { Search, Loader2, AlertCircle, ShieldAlert } from 'lucide-react';

const statusStyleMap: Record<string, string> = {
  OPEN: styles.badgeOpen,
  UNDER_INVESTIGATION: styles.badgeUnderInvestigation,
  RESOLVED: styles.badgeResolved,
  CLOSED: styles.badgeClosed,
};

const severityStyleMap: Record<string, string> = {
  CRITICAL: styles.severityCritical,
  HIGH: styles.severityHigh,
  MEDIUM: styles.severityMedium,
  LOW: styles.severityLow,
};

export const NonConformanceManagement: React.FC = () => {
  const { data: ncs, isLoading, isError } = useNonConformances();
  const [searchTerm, setSearchTerm] = useState('');

  if (isLoading) {
    return (
      <div className={styles.loadingContainer}>
        <Loader2 className={styles.spinner} size={40} />
        <span>Loading non-conformance records...</span>
      </div>
    );
  }

  if (isError) {
    return (
      <div className={styles.alertContainer}>
        <AlertCircle className={styles.errorIcon} size={48} />
        <span>Failed to load non-conformance data.</span>
      </div>
    );
  }

  const filtered = ncs?.filter(nc =>
    nc.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
    nc.type.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (nc.detectedBy || '').toLowerCase().includes(searchTerm.toLowerCase())
  ) || [];

  const openCount = ncs?.filter(nc => nc.status === 'OPEN' || nc.status === 'UNDER_INVESTIGATION').length || 0;
  const resolvedCount = ncs?.filter(nc => nc.status === 'RESOLVED' || nc.status === 'CLOSED').length || 0;

  return (
    <main className={styles.container}>
      <header className={styles.header}>
        <div className={styles.headerLeft}>
          <h1>Non-Conformance Registry</h1>
          <p>Quality assurance event tracker — OOS deviations, process failures, and corrective actions.</p>
        </div>
        <div className={styles.kpiRow}>
          <div className={`${styles.kpiChip} ${styles.kpiOpen}`}>
            <div className={styles.kpiChipValue}>{openCount}</div>
            <div className={styles.kpiChipLabel}>Open / Investigating</div>
          </div>
          <div className={`${styles.kpiChip} ${styles.kpiResolved}`}>
            <div className={styles.kpiChipValue}>{resolvedCount}</div>
            <div className={styles.kpiChipLabel}>Resolved / Closed</div>
          </div>
        </div>
      </header>

      <section className={styles.toolbar}>
        <div className={styles.searchBox}>
          <Search size={18} className={styles.searchIcon} />
          <input
            type="text"
            placeholder="Search by title, type, or detector..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className={styles.searchInput}
          />
        </div>
      </section>

      <div className={styles.tableWrapper}>
        <table className={styles.dataTable}>
          <thead>
            <tr>
              <th>#</th>
              <th>Title</th>
              <th>Type</th>
              <th>Severity</th>
              <th>Status</th>
              <th>Detected By</th>
              <th>Detected At</th>
            </tr>
          </thead>
          <tbody>
            {filtered.length === 0 ? (
              <tr>
                <td colSpan={7}>
                  <div className={styles.emptyState}>
                    <ShieldAlert size={48} className={styles.emptyIcon} />
                    <p>No non-conformances registered. The laboratory is operating within all specifications. ✅</p>
                  </div>
                </td>
              </tr>
            ) : (
              filtered.map((nc) => (
                <tr key={nc.id}>
                  <td className={styles.fw500}>{nc.id}</td>
                  <td className={styles.fw500}>{nc.title}</td>
                  <td>{nc.type.replace(/_/g, ' ')}</td>
                  <td>
                    <span className={`${styles.severityBadge} ${severityStyleMap[nc.severity] || ''}`}>
                      {nc.severity}
                    </span>
                  </td>
                  <td>
                    <span className={`${styles.badge} ${statusStyleMap[nc.status] || ''}`}>
                      {nc.status.replace(/_/g, ' ')}
                    </span>
                  </td>
                  <td>{nc.detectedBy || '—'}</td>
                  <td>{nc.detectedAt ? new Date(nc.detectedAt).toLocaleDateString() : '—'}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </main>
  );
};
