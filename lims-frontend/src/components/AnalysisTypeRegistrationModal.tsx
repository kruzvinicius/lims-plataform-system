import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Ruler, X, Loader2, AlertCircle, Pencil } from 'lucide-react';
import styles from '../styles/SampleRegistrationModal.module.css';
import { useAnalysisTypeMutation, useUpdateAnalysisTypeMutation, type AnalysisType } from '../hooks/useAnalysisTypes';

const analysisTypeSchema = z.object({
  code:             z.string().min(2, 'Code must be at least 2 characters (e.g. PH, TURB)'),
  name:             z.string().min(3, 'Name must be at least 3 characters'),
  description:      z.string().optional(),
  defaultUnit:      z.string().optional(),
  uncertaintyValue: z.string().optional(),
  defaultPrice:     z.string().optional(),
});

type AnalysisTypeFormData = z.infer<typeof analysisTypeSchema>;

interface Props {
  isOpen: boolean;
  onClose: () => void;
  editData?: AnalysisType | null;
}

export const AnalysisTypeRegistrationModal: React.FC<Props> = ({ isOpen, onClose, editData }) => {
  const createMutation = useAnalysisTypeMutation();
  const updateMutation = useUpdateAnalysisTypeMutation();
  const isEditing = !!editData?.id;
  const mutation = isEditing ? updateMutation : createMutation;

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<AnalysisTypeFormData>({
    resolver: zodResolver(analysisTypeSchema),
  });

  useEffect(() => {
    const handleEsc = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose(); };
    if (isOpen) document.addEventListener('keydown', handleEsc);
    return () => document.removeEventListener('keydown', handleEsc);
  }, [isOpen, onClose]);

  useEffect(() => {
    if (isOpen) {
      createMutation.reset();
      updateMutation.reset();
      if (editData) {
        reset({
          code: editData.code || '',
          name: editData.name || '',
          description: editData.description || '',
          defaultUnit: editData.defaultUnit || '',
          uncertaintyValue: editData.uncertaintyValue != null ? String(editData.uncertaintyValue) : '',
          defaultPrice: editData.defaultPrice != null ? String(editData.defaultPrice) : '',
        });
      } else {
        reset({ code: '', name: '', description: '', defaultUnit: '', uncertaintyValue: '', defaultPrice: '' });
      }
    }
  }, [isOpen, editData]);

  const onSubmit = async (data: AnalysisTypeFormData) => {
    const payload: AnalysisType = {
      code: data.code.toUpperCase(),
      name: data.name,
      description: data.description || undefined,
      defaultUnit: data.defaultUnit || undefined,
      uncertaintyValue: data.uncertaintyValue ? parseFloat(data.uncertaintyValue) : null,
      defaultPrice: data.defaultPrice ? parseFloat(data.defaultPrice) : null,
      active: editData?.active ?? true,
    };

    if (isEditing && editData?.id) {
      await updateMutation.mutateAsync({ id: editData.id, payload });
    } else {
      await createMutation.mutateAsync(payload);
    }
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}>
      <div className={styles.modal} role="dialog" aria-modal="true">
        <div className={styles.modalHeader}>
          <div className={styles.modalTitleGroup}>
            <div className={styles.modalIcon}>{isEditing ? <Pencil size={22} /> : <Ruler size={22} />}</div>
            <div>
              <h2 className={styles.modalTitle}>{isEditing ? 'Edit Analysis Type' : 'Register Analysis Type'}</h2>
              <p className={styles.modalSubtitle}>{isEditing ? 'Update parameter specifications' : 'Define a new test parameter with acceptance limits'}</p>
            </div>
          </div>
          <button className={styles.closeBtn} onClick={onClose}><X size={20} /></button>
        </div>

        {mutation.isError && (
          <div className={styles.errorMsg} style={{ margin: '0 0 1rem', background: 'rgba(239,68,68,0.08)', padding: '0.75rem', borderRadius: '10px' }}>
            <AlertCircle size={16} /><span>Failed to {isEditing ? 'update' : 'register'} analysis type.</span>
          </div>
        )}

        <form className={styles.form} onSubmit={handleSubmit(onSubmit)} noValidate>
          <div className={styles.fieldRow}>
            <div className={styles.fieldGroup}>
              <label className={styles.label} htmlFor="at-code">Code <span className={styles.required}>*</span></label>
              <input id="at-code" className={`${styles.input} ${errors.code ? styles.inputError : ''}`} placeholder="e.g. PH, TURB, COL-TOTAL" {...register('code')} />
              {errors.code && <span className={styles.errorMsg}><AlertCircle size={13} />{errors.code.message}</span>}
            </div>
            <div className={styles.fieldGroup}>
              <label className={styles.label} htmlFor="at-unit">Default Unit</label>
              <input id="at-unit" className={styles.input} placeholder="e.g. mg/L, NTU, UFC/mL" {...register('defaultUnit')} />
            </div>
          </div>

          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="at-name">Full Name <span className={styles.required}>*</span></label>
            <input id="at-name" className={`${styles.input} ${errors.name ? styles.inputError : ''}`} placeholder="e.g. Turbidez, Coliformes Totais" {...register('name')} />
            {errors.name && <span className={styles.errorMsg}><AlertCircle size={13} />{errors.name.message}</span>}
          </div>

          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="at-desc">Method Description</label>
            <input id="at-desc" className={styles.input} placeholder="e.g. Metodologia EPA 150.1" {...register('description')} />
          </div>

          <div className={styles.fieldRow}>
            <div className={styles.fieldGroup}>
              <label className={styles.label} htmlFor="at-uncertainty">Measurement Uncertainty (±)</label>
              <input id="at-uncertainty" type="number" step="any" className={styles.input} placeholder="e.g. 0.05" {...register('uncertaintyValue')} />
            </div>
            <div className={styles.fieldGroup}>
              <label className={styles.label} htmlFor="at-price">Default Price ($)</label>
              <input id="at-price" type="number" step="any" className={styles.input} placeholder="ex: 15.00" {...register('defaultPrice')} />
            </div>
          </div>

          <hr className={styles.divider} />

          <div className={styles.modalFooter}>
            <button type="button" className={styles.cancelBtn} onClick={onClose} disabled={isSubmitting}>Cancel</button>
            <button type="submit" className={styles.submitBtn} disabled={isSubmitting || mutation.isPending}>
              {(isSubmitting || mutation.isPending) ? (
                <><Loader2 size={16} className={styles.spinner} /> {isEditing ? 'Saving...' : 'Registering...'}</>
              ) : (
                <>{isEditing ? <Pencil size={16} /> : <Ruler size={16} />} {isEditing ? 'Save Changes' : 'Register Parameter'}</>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
