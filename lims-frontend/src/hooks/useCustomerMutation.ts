import { useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../api/axiosClient';

export interface CustomerCreatePayload {
  corporateReason: string;
  email: string;
  taxId: string;
  phone: string;
}

const createCustomer = async (payload: CustomerCreatePayload) => {
  const { data } = await apiClient.post('/customers', payload);
  return data;
};

/**
 * Mutation hook for registering a new customer.
 * On success, invalidates the customer registry cache for auto-refresh.
 */
export function useCustomerMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: createCustomer,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['customers_registry'] });
    },
  });
}
