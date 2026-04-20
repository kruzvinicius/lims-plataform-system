import React, { useEffect, useState } from 'react';
import { useForm, useFieldArray } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Scale, X, Loader2, AlertCircle, Pencil, Plus, Trash2 } from 'lucide-react';
import styles from '../styles/SampleRegistrationModal.module.css';
import {
  useCreateLegislationMutation,
  useUpdateLegislationMutation,
  type Legislation,
} from '../hooks/useLegislations';
import { useAnalysisTypes } from '../hooks/useAnalysisTypes';

const paramSchema = z.object({
  analysisTypeId: z.string().min(1, 'Select a parameter'),
  vmpMin: z.string().optional(),
  vmpMax: z.string().min(1, 'Max VMP is required'),
  unit: z.string().optional(),
  notes: z.string().optional(),
});

const legislationSchema = z.object({
  code: z.string().min(2, 'Code must be at least 2 characters'),
  name: z.string().min(3, 'Name must be at least 3 characters'),
  region: z.string().optional(),
  description: z.string().optional(),
  parameters: z.array(paramSchema),
});

type FormData = z.infer<typeof legislationSchema>;

interface Props {
  isOpen: boolean;
  onClose: () => void;
  editData?: Legislation | null;
}

export const LegislationRegistrationModal: React.FC<Props> = ({ isOpen, onClose, editData }) => {
  const createMutation = useCreateLegislationMutation();
  const updateMutation = useUpdateLegislationMutation();
  const { data: analysisTypes } = useAnalysisTypes(false);
  const isEditing = !!editData?.id;
  const mutation = isEditing ? updateMutation : createMutation;

  const { register, handleSubmit, reset, control, formState: { errors, isSubmitting } } =
    useForm<FormData>({ resolver: zodResolver(legislationSchema), defaultValues: { parameters: [] } });

  const { fields, append, remove } = useFieldArray({ control, name: 'parameters' });

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
          region: editData.region || '',
          description: editData.description || '',
          parameters: (editData.parameters || []).map(p => ({
            analysisTypeId: String(p.analysisTypeId),
            vmpMin: p.vmpMin != null ? String(p.vmpMin) : '',
            vmpMax: String(p.vmpMax),
            unit: p.unit || '',
            notes: p.notes || '',
          })),
        });
      } else {
        reset({ code: '', name: '', region: '', description: '', parameters: [] });
      }
    }
  }, [isOpen, editData]);

  const onSubmit = async (data: FormData) => {
    const payload: Legislation = {
      code: data.code.toUpperCase(),
      name: data.name,
      region: data.region || undefined,
      description: data.description || undefined,
      active: editData?.active ?? true,
      parameters: data.parameters.map(p => ({
        analysisTypeId: Number(p.analysisTypeId),
        vmpMin: p.vmpMin ? Number(p.vmpMin) : null,
        vmpMax: Number(p.vmpMax),
        unit: p.unit || undefined,
        notes: p.notes || undefined,
      })),
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
      <div className={styles.modal} role="dialog" aria-modal="true" style={{ maxWidth: '720px', maxHeight: '90vh' }}>
        <div className={styles.modalHeader}>
          <div className={styles.modalTitleGroup}>
            <div className={styles.modalIcon}>{isEditing ? <Pencil size={22} /> : <Scale size={22} />}</div>
            <div>
              <h2 className={styles.modalTitle}>{isEditing ? 'Edit Legislation' : 'New Environmental Legislation'}</h2>
              <p className={styles.modalSubtitle}>Define VMPs (Maximum Permitted Values) per analytical parameter.</p>
            </div>
          </div>
          <button className={styles.closeBtn} onClick={onClose}><X size={20} /></button>
        </div>

        {mutation.isError && (
          <div className={styles.errorMsg} style={{ margin: '0 0 1rem', background: 'rgba(239,68,68,0.08)', padding: '0.75rem', borderRadius: '10px' }}>
            <AlertCircle size={16} /><span>Failed to {isEditing ? 'update' : 'create'} legislation.</span>
          </div>
        )}

        <form className={styles.form} onSubmit={handleSubmit(onSubmit)} noValidate style={{ overflowY: 'auto' }}>
          {/* Basic Info */}
          <div className={styles.fieldRow}>
            <div className={styles.fieldGroup}>
              <label className={styles.label} htmlFor="leg-code">Code <span className={styles.required}>*</span></label>
              <input id="leg-code" className={`${styles.input} ${errors.code ? styles.inputError : ''}`}
                placeholder="e.g. CONAMA-357-II" {...register('code')} />
              {errors.code && <span className={styles.errorMsg}><AlertCircle size={13} />{errors.code.message}</span>}
            </div>
            <div className={styles.fieldGroup}>
              <label className={styles.label} htmlFor="leg-region">Region / Scope</label>
              <input id="leg-region" className={styles.input} placeholder="e.g. Brazil, São Paulo" {...register('region')} />
            </div>
          </div>

          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="leg-name">Full Name <span className={styles.required}>*</span></label>
            <input id="leg-name" className={`${styles.input} ${errors.name ? styles.inputError : ''}`}
              placeholder="e.g. CONAMA Resolution 357/2005 – Class II" {...register('name')} />
            {errors.name && <span className={styles.errorMsg}><AlertCircle size={13} />{errors.name.message}</span>}
          </div>

          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="leg-desc">Description / Notes</label>
            <input id="leg-desc" className={styles.input}
              placeholder="e.g. Classification for class II rivers" {...register('description')} />
          </div>

          <hr className={styles.divider} />

          {/* VMP Parameters */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
            <label className={styles.label} style={{ margin: 0, fontWeight: 700 }}>
              VMPs by Parameter ({fields.length})
            </label>
            <button
              type="button"
              onClick={() => append({ analysisTypeId: '', vmpMin: '', vmpMax: '', unit: '', notes: '' })}
              style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', padding: '0.4rem 0.9rem', borderRadius: '8px', border: '1px solid var(--accent)', background: 'transparent', color: 'var(--accent)', cursor: 'pointer', fontSize: '0.85rem', fontWeight: 600 }}
            >
              <Plus size={14} /> Add Parameter
            </button>
          </div>

          {fields.length === 0 && (
            <div style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '1rem', fontSize: '0.875rem', border: '1px dashed var(--border)', borderRadius: '10px', marginBottom: '1rem' }}>
              No parameters added. Use the button above to define VMPs.
            </div>
          )}

          {fields.map((field, index) => (
            <div key={field.id} style={{ display: 'grid', gridTemplateColumns: '2fr 1fr 1fr 1fr auto', gap: '0.75rem', alignItems: 'end', marginBottom: '0.75rem', padding: '0.75rem', background: 'rgba(255,255,255,0.04)', borderRadius: '10px', border: '1px solid var(--border)' }}>
              <div>
                <label className={styles.label} style={{ fontSize: '0.75rem' }}>Analytical Parameter *</label>
                <select
                  className={styles.input}
                  {...register(`parameters.${index}.analysisTypeId`)}
                  style={{ width: '100%' }}
                >
                  <option value="">Select...</option>
                  {analysisTypes?.filter(at => at.active).map(at => (
                    <option key={at.id} value={at.id}>{at.code} – {at.name}</option>
                  ))}
                </select>
                {errors.parameters?.[index]?.analysisTypeId && (
                  <span className={styles.errorMsg}><AlertCircle size={12} />{errors.parameters[index]?.analysisTypeId?.message}</span>
                )}
              </div>
              <div>
                <label className={styles.label} style={{ fontSize: '0.75rem' }}>Min (optional)</label>
                <input type="number" step="any" className={styles.input} placeholder="e.g. 6.0"
                  {...register(`parameters.${index}.vmpMin`)} />
              </div>
              <div>
                <label className={styles.label} style={{ fontSize: '0.75rem' }}>Max (VMP) *</label>
                <input type="number" step="any" className={styles.input} placeholder="e.g. 9.0"
                  {...register(`parameters.${index}.vmpMax`)} />
                {errors.parameters?.[index]?.vmpMax && (
                  <span className={styles.errorMsg}><AlertCircle size={12} />{errors.parameters[index]?.vmpMax?.message}</span>
                )}
              </div>
              <div>
                <label className={styles.label} style={{ fontSize: '0.75rem' }}>Unit</label>
                <input className={styles.input} placeholder="mg/L, NTU..." {...register(`parameters.${index}.unit`)} />
              </div>
              <button
                type="button"
                onClick={() => remove(index)}
                style={{ padding: '0.5rem', borderRadius: '8px', border: '1px solid rgba(239,68,68,0.4)', background: 'rgba(239,68,68,0.08)', color: '#f87171', cursor: 'pointer', height: '40px', alignSelf: 'flex-end', marginBottom: errors.parameters?.[index] ? '1.5rem' : '0' }}
                title="Remove"
              >
                <Trash2 size={14} />
              </button>
            </div>
          ))}

          <hr className={styles.divider} />

          <div className={styles.modalFooter}>
            <button type="button" className={styles.cancelBtn} onClick={onClose} disabled={isSubmitting}>Cancel</button>
            <button type="submit" className={styles.submitBtn} disabled={isSubmitting || mutation.isPending}>
              {(isSubmitting || mutation.isPending) ? (
                <><Loader2 size={16} className={styles.spinner} /> {isEditing ? 'Saving...' : 'Creating...'}</>
              ) : (
                <><Scale size={16} /> {isEditing ? 'Save Changes' : 'Create Legislation'}</>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
