import { useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../api/axiosClient';

export interface SampleCreatePayload {
  description: string;
  barcode: string;
  materialType: string;
  collectionLocation?: string;
  collectionDate?: string; // ISO string: "YYYY-MM-DD"
  notes?: string;
  customerId: number;
}

const createSample = async (payload: SampleCreatePayload) => {
  const { data } = await apiClient.post('/samples', payload);
  return data;
};

/**
 * Mutation hook for registering a new sample.
 * On success, automatically invalidates the live samples table cache,
 * which triggers a background re-fetch — the new row "appears" without a page reload.
 */
export function useSampleMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: createSample,
    onSuccess: () => {
      // Invalidate the shared query key so the dashboard table auto-refreshes
      queryClient.invalidateQueries({ queryKey: ['samples_live_table'] });
    },
  });
}
