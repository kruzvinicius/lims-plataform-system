import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../api/axiosClient';

// ─── DTOs ────────────────────────────────────────────────────────────────
export interface TestResultDTO {
  id?: number;
  parameterName: string;
  resultValue: string;
  unit: string;
  performedAt?: string;
  status?: string;
  approvedBy?: string;
  approvedAt?: string;
  rejectionReason?: string;
}

export interface ApprovalRequest {
  reviewerUsername: string;
  reason?: string;
}

// ─── Fetch ───────────────────────────────────────────────────────────────
const fetchSampleResults = async (sampleId: number): Promise<TestResultDTO[]> => {
  const { data } = await apiClient.get(`/samples/${sampleId}/results`);
  return data;
};

export function useSampleResults(sampleId: number | null) {
  return useQuery({
    queryKey: ['sample_results', sampleId],
    queryFn: () => fetchSampleResults(sampleId!),
    enabled: !!sampleId, // Only fetch if a sample is actually selected
  });
}

// ─── Mutations ───────────────────────────────────────────────────────────

export function useAddResultMutation(sampleId: number | null) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (payload: TestResultDTO) => {
      const { data } = await apiClient.post(`/samples/${sampleId}/results`, payload);
      return data;
    },
    onSuccess: () => {
      // Refresh the specific sample's results and the master table
      queryClient.invalidateQueries({ queryKey: ['sample_results', sampleId] });
      queryClient.invalidateQueries({ queryKey: ['samples_live_table'] });
    },
  });
}

export function useApproveSampleMutation(sampleId: number | null) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (payload: ApprovalRequest) => {
      const { data } = await apiClient.post(`/samples/${sampleId}/approve`, payload);
      return data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['samples_live_table'] });
    },
  });
}

export function useUpdateResultMutation(sampleId: number | null) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ id, value }: { id: number; value: string }) => {
      const { data } = await apiClient.put(`/results/${id}`, value, {
        headers: { 'Content-Type': 'text/plain' }
      });
      return data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['sample_results', sampleId] });
      queryClient.invalidateQueries({ queryKey: ['samples_live_table'] });
    },
  });
}

export function useRejectSampleMutation(sampleId: number | null) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (payload: ApprovalRequest) => {
      const { data } = await apiClient.post(`/samples/${sampleId}/reject`, payload);
      return data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['samples_live_table'] });
    },
  });
}

export function useUpdateStatusMutation(sampleId: number | null) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (status: string) => {
      const { data } = await apiClient.patch(`/samples/${sampleId}/status`, status, {
        headers: { 'Content-Type': 'text/plain' }
      });
      return data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['samples_live_table'] });
    },
  });
}
