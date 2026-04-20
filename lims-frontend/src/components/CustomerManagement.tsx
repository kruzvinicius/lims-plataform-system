import React, { useState } from 'react';
import { useCustomers } from '../hooks/useCustomers';
import styles from '../styles/CustomerManagement.module.css';
import { Search, Loader2, AlertCircle, Building2, Hash, Plus } from 'lucide-react';
import { CustomerRegistrationModal } from './CustomerRegistrationModal';

export const CustomerManagement: React.FC = () => {
  const { data: customers, isLoading, isError } = useCustomers();
  const [searchTerm, setSearchTerm] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);

  if (isLoading) {
    return (
      <div className={styles.loadingContainer}>
        <Loader2 className={styles.spinner} size={40} />
        <span>Loading customer registry...</span>
      </div>
    );
  }

  if (isError) {
    return (
      <div className={styles.alertContainer}>
        <AlertCircle className={styles.errorIcon} size={48} />
        <span>Failed to load customers from LIMS Backend.</span>
      </div>
    );
  }

  const filteredCustomers = customers?.filter(c =>
    c.corporateReason.toLowerCase().includes(searchTerm.toLowerCase()) ||
    c.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
    c.taxId.includes(searchTerm) ||
    c.phone.includes(searchTerm)
  ) || [];

  return (
    <main className={styles.container}>

      {/* Header */}
      <header className={styles.header}>
        <div className={styles.headerLeft}>
          <h1>Customer Registry</h1>
          <p>Manage hospitals, clinics, and direct clients for sample traceability.</p>
        </div>
        <div className={styles.headerRight}>
          <div className={styles.kpiWidget}>
            <div className={styles.kpiValue}>{customers?.length || 0}</div>
            <div className={styles.kpiLabel}>Registered Clients</div>
          </div>
        </div>
      </header>

      {/* Toolbar */}
      <section className={styles.toolbar}>
        <div className={styles.searchBox}>
          <Search size={18} className={styles.searchIcon} />
          <input
            type="text"
            placeholder="Search by name, email, CNPJ, or phone..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className={styles.searchInput}
          />
        </div>
        <button
          id="register-customer-btn"
          className={styles.registerBtn}
          onClick={() => setIsModalOpen(true)}
        >
          <Plus size={18} /> Register Customer
        </button>
      </section>

      <CustomerRegistrationModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} />

      {/* Table */}
      <div className={styles.tableWrapper}>
        <table className={styles.dataTable}>
          <thead>
            <tr>
              <th><Hash size={16} /> ID</th>
              <th><Building2 size={16} /> Corporate Name</th>
              <th>Email</th>
              <th>CNPJ / Tax ID</th>
              <th>Phone</th>
            </tr>
          </thead>
          <tbody>
            {filteredCustomers.length === 0 ? (
              <tr>
                <td colSpan={5}>
                  <div className={styles.emptyState}>
                    <Building2 size={48} className={styles.emptyIcon} />
                    <p>No customers registered yet. Click <strong>+ Register Customer</strong> to add the first one.</p>
                  </div>
                </td>
              </tr>
            ) : (
              filteredCustomers.map((customer) => (
                <tr key={customer.id}>
                  <td className={styles.idColumn}>{customer.id}</td>
                  <td className={styles.fw500}>{customer.corporateReason}</td>
                  <td className={styles.emailColumn}>{customer.email}</td>
                  <td className={styles.taxIdColumn}>{customer.taxId}</td>
                  <td>{customer.phone}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </main>
  );
};
