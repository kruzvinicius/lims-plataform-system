import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Building2, X, Loader2, AlertCircle } from 'lucide-react';
import styles from '../styles/SampleRegistrationModal.module.css'; // Reuse same modal styling
import { useCustomerMutation } from '../hooks/useCustomerMutation';

// ─── Zod Schema (mirrors Java CustomerRequest) ───────────────────────────────
const customerSchema = z.object({
  corporateReason: z.string().min(3, 'Corporate name must be at least 3 characters'),
  email:           z.string().email('Please enter a valid email address'),
  taxId:           z.string().min(5, 'Tax ID / CNPJ must be at least 5 characters'),
  phone:           z.string().min(8, 'Phone must be at least 8 characters'),
});

type CustomerFormData = z.infer<typeof customerSchema>;

// ─── Props ────────────────────────────────────────────────────────────────────
interface CustomerRegistrationModalProps {
  isOpen: boolean;
  onClose: () => void;
}

// ─── Component ────────────────────────────────────────────────────────────────
export const CustomerRegistrationModal: React.FC<CustomerRegistrationModalProps> = ({
  isOpen,
  onClose,
}) => {
  const mutation = useCustomerMutation();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<CustomerFormData>({
    resolver: zodResolver(customerSchema),
  });

  // Close on Escape key
  useEffect(() => {
    const handleEsc = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose(); };
    if (isOpen) document.addEventListener('keydown', handleEsc);
    return () => document.removeEventListener('keydown', handleEsc);
  }, [isOpen, onClose]);

  // Reset form when modal opens
  useEffect(() => {
    if (isOpen) {
      reset();
      mutation.reset();
    }
  }, [isOpen]);

  const onSubmit = async (data: CustomerFormData) => {
    await mutation.mutateAsync(data);
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}>
      <div className={styles.modal} role="dialog" aria-modal="true" aria-labelledby="customer-modal-title">

        {/* Header */}
        <div className={styles.modalHeader}>
          <div className={styles.modalTitleGroup}>
            <div className={styles.modalIcon}>
              <Building2 size={22} />
            </div>
            <div>
              <h2 id="customer-modal-title" className={styles.modalTitle}>Register Customer</h2>
              <p className={styles.modalSubtitle}>Add a new hospital, clinic, or direct client</p>
            </div>
          </div>
          <button className={styles.closeBtn} onClick={onClose} aria-label="Close modal">
            <X size={20} />
          </button>
        </div>

        {/* Error Banner */}
        {mutation.isError && (
          <div className={styles.errorMsg} style={{ marginBottom: '1rem', background: 'rgba(239,68,68,0.08)', padding: '0.75rem', borderRadius: '10px' }}>
            <AlertCircle size={16} />
            <span>Failed to register customer. Check your connection and try again.</span>
          </div>
        )}

        {/* Form */}
        <form className={styles.form} onSubmit={handleSubmit(onSubmit)} noValidate>

          {/* Corporate Reason */}
          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="corporateReason">
              Corporate Name / Razão Social <span className={styles.required}>*</span>
            </label>
            <input
              id="corporateReason"
              className={`${styles.input} ${errors.corporateReason ? styles.inputError : ''}`}
              placeholder="e.g. Hospital São Paulo LTDA"
              {...register('corporateReason')}
            />
            {errors.corporateReason && <span className={styles.errorMsg}><AlertCircle size={13} />{errors.corporateReason.message}</span>}
          </div>

          {/* Email + Phone */}
          <div className={styles.fieldRow}>
            <div className={styles.fieldGroup}>
              <label className={styles.label} htmlFor="email">
                Email <span className={styles.required}>*</span>
              </label>
              <input
                id="email"
                type="email"
                className={`${styles.input} ${errors.email ? styles.inputError : ''}`}
                placeholder="contact@hospital.com"
                {...register('email')}
              />
              {errors.email && <span className={styles.errorMsg}><AlertCircle size={13} />{errors.email.message}</span>}
            </div>

            <div className={styles.fieldGroup}>
              <label className={styles.label} htmlFor="phone">
                Phone <span className={styles.required}>*</span>
              </label>
              <input
                id="phone"
                type="tel"
                className={`${styles.input} ${errors.phone ? styles.inputError : ''}`}
                placeholder="(11) 98765-4321"
                {...register('phone')}
              />
              {errors.phone && <span className={styles.errorMsg}><AlertCircle size={13} />{errors.phone.message}</span>}
            </div>
          </div>

          {/* Tax ID / CNPJ */}
          <div className={styles.fieldGroup}>
            <label className={styles.label} htmlFor="taxId">
              CNPJ / Tax ID <span className={styles.required}>*</span>
            </label>
            <input
              id="taxId"
              className={`${styles.input} ${errors.taxId ? styles.inputError : ''}`}
              placeholder="12.345.678/0001-90"
              {...register('taxId')}
            />
            {errors.taxId && <span className={styles.errorMsg}><AlertCircle size={13} />{errors.taxId.message}</span>}
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
                <><Building2 size={16} /> Register Customer</>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
