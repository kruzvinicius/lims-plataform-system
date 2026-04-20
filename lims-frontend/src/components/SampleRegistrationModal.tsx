import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { FlaskConical, X, Loader2, Shuffle, AlertCircle } from 'lucide-react';
import styles from '../styles/SampleRegistrationModal.module.css';
import { useSampleMutation } from '../hooks/useSampleMutation';
import { useCustomers } from '../hooks/useCustomers';

// ─── Zod Validation Schema (mirrors Java SampleRequest) ───────────────────────
const sampleSchema = z.object({
  description:        z.string().min(3, 'Description must be at least 3 characters'),
  barcode:            z.string().min(4, 'Barcode must be at least 4 characters'),
  materialType:       z.string().min(2, 'Material type is required'),
  collectionLocation: z.string().optional(),
  collectionDate:     z.string().optional(),
  notes:              z.string().optional(),
  customerId:         z.string()
                       .min(1, 'Customer ID is required')
                       .refine(v => !isNaN(Number(v)) && Number(v) > 0, {
                         message: 'Customer ID must be a positive number',
                       }),
});

type SampleFormData = z.infer<typeof sampleSchema>;

// We convert customerId before sending to the API
const toPayload = (data: SampleFormData) => ({
  ...data,
  customerId: Number(data.customerId),
});

// ─── Helpers ──────────────────────────────────────────────────────────────────
const generateBarcode = (): string => {
  const prefix = 'LIMS';
  const timestamp = Date.now().toString().slice(-7);
  const suffix = Math.random().toString(36).substring(2, 5).toUpperCase();
  return `${prefix}-${timestamp}-${suffix}`;
};

// ─── Props ────────────────────────────────────────────────────────────────────
interface SampleRegistrationModalProps {
  isOpen: boolean;
  onClose: () => void;
}

// ─── Component ────────────────────────────────────────────────────────────────
export const SampleRegistrationModal: React.FC<SampleRegistrationModalProps> = ({
  isOpen,
  onClose,
}) => {
  const mutation = useSampleMutation();
  const { data: customers } = useCustomers();

  const {
    register,
    handleSubmit,
    setValue,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<SampleFormData>({
    resolver: zodResolver(sampleSchema),
    defaultValues: {
      barcode: generateBarcode(),
      collectionDate: new Date().toISOString().split('T')[0],
    },
  });

  // Close on Escape key
  useEffect(() => {
    const handleEsc = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose(); };
    if (isOpen) { document.addEventListener('keydown', handleEsc); }
    return () => document.removeEventListener('keydown', handleEsc);
  }, [isOpen, onClose]);

  // Reset form when modal opens
  useEffect(() => {
    if (isOpen) {
      reset({
        barcode: generateBarcode(),
        collectionDate: new Date().toISOString().split('T')[0],
      });
      mutation.reset();
    }
  }, [isOpen]);

  const onSubmit = async (data: SampleFormData) => {
    await mutation.mutateAsync(toPayload(data));
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}>
      <div className={styles.modal} role="dialog" aria-modal="true" aria-labelledby="modal-title">

        {/* ── Header ─────────────────────────────────────────────── */}
        <div className={styles.modalHeader}>
          <div className={styles.modalTitleGroup}>
            <div className={styles.modalIcon}>
              <FlaskConical size={22} />
            </div>
            <div>
              <h2 id="modal-title" className={styles.modalTitle}>Register New Sample</h2>
              <p className={styles.modalSubtitle}>Fill in the clinical intake form</p>
            </div>
          </div>
          <button className={styles.closeBtn} onClick={onClose} aria-label="Close modal">
            <X size={20} />
          </button>
        </div>

        {/* ── Error Banner ────────────────────────────────────────── */}
        {mutation.isError && (
          <div className={styles.errorMsg} style={{ marginBottom: '1rem', background: 'rgba(239,68,68,0.08)', padding: '0.75rem', borderRadius: '10px' }}>
            <AlertCircle size={16} />
            <span>Failed to register sample. Check your connection and try again.</span>
          </div>
        )}

        {/* ── Form ────────────────────────────────────────────────── */}
        <form className={styles.form} onSubmit={handleSubmit(onSubmit)} noValidate>

          {/* Description */}
          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="description">
              Sample Description <span className={styles.required}>*</span>
            </label>
            <input
              id="description"
              className={`${styles.input} ${errors.description ? styles.inputError : ''}`}
              placeholder="e.g. Blood CBC — Patient intake"
              {...register('description')}
            />
            {errors.description && <span className={styles.errorMsg}><AlertCircle size={13} />{errors.description.message}</span>}
          </div>

          {/* Barcode + Material Type */}
          <div className={styles.fieldRow}>
            <div className={styles.fieldGroup}>
              <label className={styles.label} htmlFor="barcode">
                Barcode <span className={styles.required}>*</span>
              </label>
              <div className={styles.barcodeWrapper}>
                <input
                  id="barcode"
                  className={`${styles.input} ${styles.barcodeInput} ${errors.barcode ? styles.inputError : ''}`}
                  placeholder="LIMS-XXXXXXX-XXX"
                  {...register('barcode')}
                />
                <button
                  type="button"
                  className={styles.generateBtn}
                  title="Auto-generate barcode"
                  onClick={() => setValue('barcode', generateBarcode(), { shouldValidate: true })}
                >
                  <Shuffle size={16} />
                </button>
              </div>
              {errors.barcode && <span className={styles.errorMsg}><AlertCircle size={13} />{errors.barcode.message}</span>}
            </div>

            <div className={styles.fieldGroup}>
              <label className={styles.label} htmlFor="materialType">
                Material Type <span className={styles.required}>*</span>
              </label>
              <select
                id="materialType"
                className={`${styles.select} ${errors.materialType ? styles.inputError : ''}`}
                {...register('materialType')}
                defaultValue=""
              >
                <option value="" disabled>Select type...</option>
                <option value="Blood">Blood</option>
                <option value="Urine">Urine</option>
                <option value="Serum">Serum</option>
                <option value="Plasma">Plasma</option>
                <option value="Tissue">Tissue</option>
                <option value="Swab">Swab</option>
                <option value="Stool">Stool</option>
                <option value="CSF">CSF</option>
                <option value="Other">Other</option>
              </select>
              {errors.materialType && <span className={styles.errorMsg}><AlertCircle size={13} />{errors.materialType.message}</span>}
            </div>
          </div>

          {/* Collection Date + Location */}
          <div className={styles.fieldRow}>
            <div className={styles.fieldGroup}>
              <label className={styles.label} htmlFor="collectionDate">Collection Date</label>
              <input
                id="collectionDate"
                type="date"
                className={styles.input}
                {...register('collectionDate')}
              />
            </div>
            <div className={styles.fieldGroup}>
              <label className={styles.label} htmlFor="collectionLocation">Collection Location</label>
              <input
                id="collectionLocation"
                className={styles.input}
                placeholder="e.g. Ward 3 — Bed 12"
                {...register('collectionLocation')}
              />
            </div>
          </div>

          {/* Customer Selection */}
          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="customerId">
              Customer / Payer <span className={styles.required}>*</span>
            </label>
            <select
              id="customerId"
              className={`${styles.select} ${errors.customerId ? styles.inputError : ''}`}
              {...register('customerId')}
              defaultValue=""
            >
              <option value="" disabled>Select a customer...</option>
              {customers?.map(customer => (
                <option key={customer.id} value={customer.id}>
                  {customer.corporateReason} ({customer.taxId})
                </option>
              ))}
            </select>
            {errors.customerId && <span className={styles.errorMsg}><AlertCircle size={13} />{errors.customerId.message}</span>}
          </div>

          {/* Notes */}
          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="notes">Clinical Notes</label>
            <textarea
              id="notes"
              className={styles.textarea}
              placeholder="Optional — fasting status, medications, special handling..."
              {...register('notes')}
            />
          </div>

          <hr className={styles.divider} />

          {/* Footer */}
          <div className={styles.modalFooter}>
            <button type="button" className={styles.cancelBtn} onClick={onClose} disabled={isSubmitting}>
              Cancel
            </button>
            <button type="submit" className={styles.submitBtn} disabled={isSubmitting || mutation.isPending}>
              {(isSubmitting || mutation.isPending) ? (
                <><Loader2 size={16} className={styles.spinner} /> Registering...</>
              ) : (
                <><FlaskConical size={16} /> Register Sample</>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
