import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../api/axiosClient';

export interface AnalysisType {
  id?: number;
  code: string;
  name: string;
  description?: string;
  defaultUnit?: string;
  /** Expanded measurement uncertainty (±) of the analytical method. */
  uncertaintyValue?: number | null;
  defaultPrice?: number | null;
  active?: boolean;
}

const fetchActiveAnalysisTypes = async (): Promise<AnalysisType[]> => {
  const { data } = await apiClient.get('/analysis-types/active');
  return data;
};

const fetchAllAnalysisTypes = async (): Promise<AnalysisType[]> => {
  const { data } = await apiClient.get('/analysis-types');
  return data;
};

export function useAnalysisTypes(activeOnly = true) {
  return useQuery({
    queryKey: ['analysis_types', activeOnly ? 'active' : 'all'],
    queryFn: activeOnly ? fetchActiveAnalysisTypes : fetchAllAnalysisTypes,
    staleTime: 1000 * 60 * 10,
  });
}

export function useAnalysisTypeMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (payload: AnalysisType) => {
      const { data } = await apiClient.post('/analysis-types', payload);
      return data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['analysis_types'] });
    },
  });
}

export function useUpdateAnalysisTypeMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ id, payload }: { id: number; payload: AnalysisType }) => {
      const { data } = await apiClient.put(`/analysis-types/${id}`, payload);
      return data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['analysis_types'] });
    },
  });
}

export function useDeleteAnalysisTypeMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: number) => {
      await apiClient.delete(`/analysis-types/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['analysis_types'] });
    },
  });
}
