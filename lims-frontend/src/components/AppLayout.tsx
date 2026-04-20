import React from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import styles from '../styles/Sidebar.module.css';

/**
 * AppLayout wraps all authenticated pages.
 * It renders the fixed Sidebar on the left and the routed page content on the right.
 */
export const AppLayout: React.FC = () => {
  return (
    <div className={styles.appLayout}>
      <Sidebar />
      <main className={styles.mainContent}>
        <Outlet />
      </main>
    </div>
  );
};
