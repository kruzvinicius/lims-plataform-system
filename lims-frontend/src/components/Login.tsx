import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { login } from '../services/authService';
import styles from '../styles/Login.module.css';
import { Lock, User, FlaskConical, AlertCircle, Loader2 } from 'lucide-react';

export const Login: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errorMsg, setErrorMsg] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username || !password) {
      setErrorMsg('Please enter both username and password.');
      return;
    }

    try {
      setIsLoading(true);
      setErrorMsg('');
      await login(username, password);
      // Successful login, token is saved in localStorage. Navigate to dashboard.
      navigate('/dashboard');
    } catch (err: any) {
      if (err.response?.status === 401 || err.response?.status === 403) {
        setErrorMsg('Invalid credentials or unauthorized access.');
      } else {
        setErrorMsg('System error communicating with the authentication server.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={styles.loginContainer}>
      <div className={styles.loginCard}>
        <header className={styles.header}>
          <div className={styles.iconWrapper}>
            <FlaskConical size={32} />
          </div>
          <h1 className={styles.title}>LIMS Access</h1>
          <p className={styles.subtitle}>Secure Laboratory Operations Portal</p>
        </header>

        <form onSubmit={handleLogin} className={styles.form}>
          <div className={styles.inputGroup}>
            <label className={styles.label}>Operator ID</label>
            <div className={styles.inputWrapper}>
              <User size={18} className={styles.inputIcon} />
              <input
                type="text"
                className={styles.input}
                placeholder="Enter your credential..."
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                autoComplete="username"
              />
            </div>
          </div>

          <div className={styles.inputGroup}>
            <label className={styles.label}>Security Key</label>
            <div className={styles.inputWrapper}>
              <Lock size={18} className={styles.inputIcon} />
              <input
                type="password"
                className={styles.input}
                placeholder="Enter your security sequence..."
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
              />
            </div>
          </div>

          {errorMsg && (
            <div className={styles.errorBox}>
              <AlertCircle size={18} />
              <span>{errorMsg}</span>
            </div>
          )}

          <button 
            type="submit" 
            className={`${styles.submitBtn} ${isLoading ? styles.loading : ''}`}
            disabled={isLoading}
          >
            {isLoading ? <Loader2 size={18} className={styles.spinner} /> : 'Authenticate'}
          </button>
        </form>
      </div>
    </div>
  );
};
