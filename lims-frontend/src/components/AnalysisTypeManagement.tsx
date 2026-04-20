import React, { useState } from 'react';
import { useAnalysisTypes, useDeleteAnalysisTypeMutation, type AnalysisType } from '../hooks/useAnalysisTypes';
import styles from '../styles/AnalysisTypeManagement.module.css';
import { Search, Loader2, AlertCircle, Ruler, Plus, ArrowUpDown, Pencil, Trash2 } from 'lucide-react';
import { AnalysisTypeRegistrationModal } from './AnalysisTypeRegistrationModal';

export const AnalysisTypeManagement: React.FC = () => {
  const { data: types, isLoading, isError } = useAnalysisTypes(false);
  const deleteMutation = useDeleteAnalysisTypeMutation();
  const [searchTerm, setSearchTerm] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingType, setEditingType] = useState<AnalysisType | null>(null);

  if (isLoading) {
    return (
      <div className={styles.loadingContainer}>
        <Loader2 className={styles.spinner} size={40} />
        <span>Loading analysis parameters...</span>
      </div>
    );
  }

  if (isError) {
    return (
      <div className={styles.alertContainer}>
        <AlertCircle className={styles.errorIcon} size={48} />
        <span>Failed to load analysis types.</span>
      </div>
    );
  }

  const filtered = types?.filter(t =>
    t.code.toLowerCase().includes(searchTerm.toLowerCase()) ||
    t.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (t.defaultUnit || '').toLowerCase().includes(searchTerm.toLowerCase())
  ) || [];

  const activeCount = types?.filter(t => t.active).length || 0;

  const formatUncertainty = (u?: number | null, unit?: string) => {
    if (u == null) return { text: 'Not defined', hasPrecision: false };
    const unitStr = unit ? ` ${unit}` : '';
    return { text: `±${u}${unitStr}`, hasPrecision: true };
  };

  const handleEdit = (at: AnalysisType) => {
    setEditingType(at);
    setIsModalOpen(true);
  };

  const handleDelete = async (at: AnalysisType) => {
    if (window.confirm(`Are you sure you want to delete the parameter "${at.name}" (${at.code})? This action cannot be undone.`)) {
      await deleteMutation.mutateAsync(at.id!);
    }
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingType(null);
  };

  const handleOpenNew = () => {
    setEditingType(null);
    setIsModalOpen(true);
  };

  return (
    <main className={styles.container}>
      <header className={styles.header}>
        <div className={styles.headerLeft}>
          <h1>Analysis Type Registry</h1>
          <p>Define laboratory parameters with acceptance specification limits (ISO 17025).</p>
        </div>
        <div className={styles.kpiRow}>
          <div className={styles.kpiChip}>
            <div className={styles.kpiChipValue}>{types?.length || 0}</div>
            <div className={styles.kpiChipLabel}>Total Parameters</div>
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
            placeholder="Search by code, name, or unit..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className={styles.searchInput}
          />
        </div>
        <button className={styles.registerBtn} onClick={handleOpenNew}>
          <Plus size={18} /> Register Parameter
        </button>
      </section>

      <AnalysisTypeRegistrationModal
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        editData={editingType}
      />

      <div className={styles.tableWrapper}>
        <table className={styles.dataTable}>
          <thead>
            <tr>
              <th><Ruler size={16} /> Code</th>
              <th>Name</th>
              <th>Method</th>
              <th>Unit</th>
              <th><ArrowUpDown size={16} /> Uncertainty (±)</th>
              <th>Default Price</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filtered.length === 0 ? (
              <tr>
                <td colSpan={8}>
                  <div className={styles.emptyState}>
                    <Ruler size={48} className={styles.emptyIcon} />
                    <p>No analysis parameters registered yet. Click <strong>+ Register Parameter</strong> to define specifications.</p>
                  </div>
                </td>
              </tr>
            ) : (
              filtered.map((at) => {
                const { text: uncText, hasPrecision } = formatUncertainty(at.uncertaintyValue, at.defaultUnit ?? undefined);
                return (
                  <tr key={at.id}>
                    <td className={styles.codeColumn}>{at.code}</td>
                    <td className={styles.fw500}>{at.name}</td>
                    <td>{at.description || '—'}</td>
                    <td className={styles.unitColumn}>{at.defaultUnit || '—'}</td>
                    <td>
                      <span className={`${styles.limitRange} ${!hasPrecision ? styles.none : ''}`}>
                        {uncText}
                      </span>
                    </td>
                    <td className={styles.priceColumn}>
                      {at.defaultPrice != null ? `$ ${at.defaultPrice.toFixed(2)}` : '—'}
                    </td>
                    <td>
                      <span className={`${styles.activeBadge} ${at.active ? styles.yes : styles.no}`}>
                        {at.active ? '● Active' : '○ Inactive'}
                      </span>
                    </td>
                    <td>
                      <div className={styles.actionGroup}>
                        <button
                          className={styles.editBtn}
                          onClick={() => handleEdit(at)}
                          title="Edit parameter"
                        >
                          <Pencil size={14} />
                        </button>
                        <button
                          className={styles.deleteBtn}
                          onClick={() => handleDelete(at)}
                          disabled={deleteMutation.isPending}
                          title="Delete parameter"
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </main>
  );
};
