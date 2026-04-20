import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../api/axiosClient';

export interface Customer {
  id: number;
  corporateReason: string;
  email: string;
  taxId: string;
  phone: string;
}

const fetchCustomers = async (): Promise<Customer[]> => {
  const { data } = await apiClient.get('/customers');
  return data;
};

/**
 * Hook for fetching all registered customers.
 * Keeps data fresh for 5 minutes and auto-refetches on tab focus.
 */
export function useCustomers() {
  return useQuery({
    queryKey: ['customers_registry'],
    queryFn: fetchCustomers,
    staleTime: 1000 * 60 * 5,
    refetchOnWindowFocus: true,
  });
}
