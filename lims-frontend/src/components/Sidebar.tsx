import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { FlaskConical, Building2, LogOut, Microscope, Ruler, ShieldAlert, ShoppingCart, Scale } from 'lucide-react';
import { logout } from '../services/authService';
import styles from '../styles/Sidebar.module.css';

interface NavItem {
  label: string;
  path: string;
  icon: React.ReactNode;
}

const navItems: NavItem[] = [
  { label: 'Sample Management', path: '/dashboard', icon: <FlaskConical size={18} /> },
  { label: 'Sales CRM', path: '/sales', icon: <ShoppingCart size={18} /> },
  { label: 'Analysis Types',           path: '/analysis-types',  icon: <Ruler size={18} /> },
  { label: 'Environmental Legislation', path: '/legislations',     icon: <Scale size={18} /> },
  { label: 'Non-Conformances',          path: '/non-conformances', icon: <ShieldAlert size={18} /> },
  { label: 'Customer Registry',         path: '/customers',        icon: <Building2 size={18} /> },
];

export const Sidebar: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();

  return (
    <aside className={styles.sidebar}>

      {/* Logo */}
      <div className={styles.logoSection}>
        <div className={styles.logoRow}>
          <div className={styles.logoIcon}>
            <Microscope size={22} />
          </div>
          <div>
            <div className={styles.logoText}>LIMS</div>
            <div className={styles.logoSubtext}>Lab Information System</div>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <nav className={styles.nav}>
        <div className={styles.navLabel}>Modules</div>
        {navItems.map((item) => (
          <button
            key={item.path}
            className={`${styles.navLink} ${location.pathname === item.path ? styles.navLinkActive : ''}`}
            onClick={() => navigate(item.path)}
          >
            <span className={styles.navIcon}>{item.icon}</span>
            {item.label}
          </button>
        ))}
      </nav>

      {/* Footer */}
      <div className={styles.footer}>
        <div className={styles.operatorCard}>
          <div className={styles.operatorAvatar}>OP</div>
          <div className={styles.operatorInfo}>
            <div className={styles.operatorName}>Operator</div>
            <div className={styles.operatorRole}>Admin</div>
          </div>
          <button className={styles.logoutBtn} onClick={logout} title="Disconnect">
            <LogOut size={16} />
          </button>
        </div>
      </div>
    </aside>
  );
};
