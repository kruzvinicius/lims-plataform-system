import React, { useState } from 'react';
import { Scale, Plus, Search, Loader2, AlertCircle, Pencil, Trash2, BookOpen } from 'lucide-react';
import { useLegislations, useDeleteLegislationMutation, type Legislation } from '../hooks/useLegislations';
import { LegislationRegistrationModal } from './LegislationRegistrationModal';
import styles from '../styles/AnalysisTypeManagement.module.css';

export const LegislationManagement: React.FC = () => {
  const { data: legislations, isLoading, isError } = useLegislations(false);
  const deleteMutation = useDeleteLegislationMutation();
  const [searchTerm, setSearchTerm] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingLeg, setEditingLeg] = useState<Legislation | null>(null);

  if (isLoading) {
    return (
      <div className={styles.loadingContainer}>
        <Loader2 className={styles.spinner} size={40} />
        <span>Loading environmental legislations...</span>
      </div>
    );
  }

  if (isError) {
    return (
      <div className={styles.alertContainer}>
        <AlertCircle className={styles.errorIcon} size={48} />
        <span>Failed to load legislations.</span>
      </div>
    );
  }

  const filtered = legislations?.filter(l =>
    l.code.toLowerCase().includes(searchTerm.toLowerCase()) ||
    l.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (l.region || '').toLowerCase().includes(searchTerm.toLowerCase())
  ) || [];

  const activeCount = legislations?.filter(l => l.active).length || 0;

  const handleEdit = (leg: Legislation) => {
    setEditingLeg(leg);
    setIsModalOpen(true);
  };

  const handleDelete = async (leg: Legislation) => {
    if (window.confirm(`Delete legislation "${leg.name}" (${leg.code})? This action cannot be undone.`)) {
      await deleteMutation.mutateAsync(leg.id!);
    }
  };

  const handleClose = () => {
    setIsModalOpen(false);
    setEditingLeg(null);
  };

  return (
    <main className={styles.container}>
      <header className={styles.header}>
        <div className={styles.headerLeft}>
          <h1>Environmental Legislations</h1>
          <p>Register environmental standards (e.g., CONAMA, Ordinances) and define VMPs per analytical parameter.</p>
        </div>
        <div className={styles.kpiRow}>
          <div className={styles.kpiChip}>
            <div className={styles.kpiChipValue}>{legislations?.length || 0}</div>
            <div className={styles.kpiChipLabel}>Total</div>
          </div>
          <div className={styles.kpiChip}>
            <div className={styles.kpiChipValue}>{activeCount}</div>
            <div className={styles.kpiChipLabel}>Active</div>
          </div>
        </div>
      </header>

      <section className={styles.toolbar}>
        <div className={styles.searchBox}>
          <Search size={18} className={styles.searchIcon} />
          <input
            type="text"
            placeholder="Search by code, name or region..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className={styles.searchInput}
          />
        </div>
        <button className={styles.registerBtn} onClick={() => { setEditingLeg(null); setIsModalOpen(true); }}>
          <Plus size={18} /> New Legislation
        </button>
      </section>

      <LegislationRegistrationModal isOpen={isModalOpen} onClose={handleClose} editData={editingLeg} />

      <div className={styles.tableWrapper}>
        <table className={styles.dataTable}>
          <thead>
            <tr>
              <th><Scale size={16} /> Code</th>
              <th>Name / Standard</th>
              <th>Region</th>
              <th>Parameters with VMP</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filtered.length === 0 ? (
              <tr>
                <td colSpan={6}>
                  <div className={styles.emptyState}>
                    <BookOpen size={48} className={styles.emptyIcon} />
                    <p>No legislations registered yet. Click <strong>+ New Legislation</strong> to get started.</p>
                  </div>
                </td>
              </tr>
            ) : (
              filtered.map((leg) => (
                <tr key={leg.id}>
                  <td className={styles.codeColumn}>{leg.code}</td>
                  <td className={styles.fw500}>{leg.name}</td>
                  <td>{leg.region || '—'}</td>
                  <td>
                    <span style={{ 
                      background: 'rgba(59,130,246,0.12)', 
                      color: '#60a5fa', 
                      borderRadius: '8px', 
                      padding: '2px 10px', 
                      fontWeight: 600, 
                      fontSize: '0.85rem' 
                    }}>
                      {leg.parameters?.length || 0} parameter(s)
                    </span>
                  </td>
                  <td>
                    <span className={`${styles.activeBadge} ${leg.active ? styles.yes : styles.no}`}>
                      {leg.active ? '● Active' : '○ Inactive'}
                    </span>
                  </td>
                  <td>
                    <div className={styles.actionGroup}>
                      <button className={styles.editBtn} onClick={() => handleEdit(leg)} title="Edit">
                        <Pencil size={14} />
                      </button>
                      <button
                        className={styles.deleteBtn}
                        onClick={() => handleDelete(leg)}
                        disabled={deleteMutation.isPending}
                        title="Delete"
                      >
                        <Trash2 size={14} />
                      </button>
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
