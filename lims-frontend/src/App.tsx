import React, { Component, type ReactNode } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { SampleManagement } from './components/SampleManagement';
import { CustomerManagement } from './components/CustomerManagement';
import { AnalysisTypeManagement } from './components/AnalysisTypeManagement';
import { NonConformanceManagement } from './components/NonConformanceManagement';
import { ProposalManagement } from './components/ProposalManagement';
import { LegislationManagement } from './components/LegislationManagement';
import { Login } from './components/Login';
import { AppLayout } from './components/AppLayout';
import { isAuthenticated } from './services/authService';

interface Props {
  children?: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
    error: null
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  public componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('Uncaught error:', error, errorInfo);
  }

  public render() {
    if (this.state.hasError) {
      return (
        <div style={{ padding: '2rem', background: '#fee2e2', color: '#991b1b', fontFamily: 'system-ui' }}>
          <h2>React Runtime Crash!</h2>
          <pre style={{ background: '#fecaca', padding: '1rem', overflowX: 'auto' }}>
            {this.state.error?.toString()}
          </pre>
          <pre style={{ background: '#fecaca', padding: '1rem', marginTop: '1rem', overflowX: 'auto' }}>
            {this.state.error?.stack}
          </pre>
        </div>
      );
    }
    return this.props.children;
  }
}

const ProtectedRoute = ({ children }: { children: React.ReactElement }) => {
  if (!isAuthenticated()) {
    return <Navigate to="/login" replace />;
  }
  return children;
};

const PublicRoute = ({ children }: { children: React.ReactElement }) => {
  if (isAuthenticated()) {
    return <Navigate to="/dashboard" replace />;
  }
  return children;
};

function App() {
  return (
    <ErrorBoundary>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={
            <PublicRoute>
              <Login />
            </PublicRoute>
          } />
          {/* Protected layout with Sidebar */}
          <Route element={
            <ProtectedRoute>
              <AppLayout />
            </ProtectedRoute>
          }>
            <Route path="/dashboard" element={<SampleManagement />} />
            <Route path="/sales" element={<ProposalManagement />} />
            <Route path="/analysis-types" element={<AnalysisTypeManagement />} />
            <Route path="/legislations" element={<LegislationManagement />} />
            <Route path="/non-conformances" element={<NonConformanceManagement />} />
            <Route path="/customers" element={<CustomerManagement />} />
          </Route>
          {/* Default redirect */}
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </ErrorBoundary>
  );
}

export default App;
