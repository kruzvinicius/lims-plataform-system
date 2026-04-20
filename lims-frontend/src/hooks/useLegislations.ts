import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../api/axiosClient';

export interface LegislationParameter {
  id?: number;
  analysisTypeId: number;
  analysisTypeCode?: string;
  analysisTypeName?: string;
  analysisTypeUnit?: string;
  /** Minimum permitted value (optional, e.g. pH >= 6.0). */
  vmpMin?: number | null;
  /** Maximum permitted value (required, e.g. pH <= 9.0 or Turbidity <= 100 NTU). */
  vmpMax: number;
  unit?: string;
  notes?: string;
}

export interface Legislation {
  id?: number;
  code: string;
  name: string;
  region?: string;
  description?: string;
  active?: boolean;
  parameters: LegislationParameter[];
}

// --- Queries ---

export function useLegislations(activeOnly = false) {
  return useQuery<Legislation[], Error>({
    queryKey: ['legislations', activeOnly ? 'active' : 'all'],
    queryFn: async () => {
      const url = activeOnly ? '/legislations/active' : '/legislations';
      const { data } = await apiClient.get(url);
      return data;
    },
    staleTime: 1000 * 60 * 5,
  });
}

// --- Mutations ---

export function useCreateLegislationMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: Legislation) => {
      const { data } = await apiClient.post('/legislations', payload);
      return data as Legislation;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['legislations'] }),
  });
}

export function useUpdateLegislationMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, payload }: { id: number; payload: Legislation }) => {
      const { data } = await apiClient.put(`/legislations/${id}`, payload);
      return data as Legislation;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['legislations'] }),
  });
}

export function useDeleteLegislationMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: number) => {
      await apiClient.delete(`/legislations/${id}`);
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['legislations'] }),
  });
}
