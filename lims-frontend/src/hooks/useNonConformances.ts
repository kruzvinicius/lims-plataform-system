import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../api/axiosClient';

export interface NonConformance {
  id: number;
  title: string;
  description: string;
  type: string;
  severity: string;
  status: string;
  detectedAt: string;
  resolvedAt?: string;
  rootCause?: string;
  correctiveAction?: string;
  preventiveAction?: string;
  detectedBy?: string;
  assignedTo?: string;
  sampleId?: number;
  testResultId?: number;
}

const fetchNonConformances = async (): Promise<NonConformance[]> => {
  const { data } = await apiClient.get('/nonconformances');
  return data;
};

export function useNonConformances() {
  return useQuery({
    queryKey: ['nonconformances'],
    queryFn: fetchNonConformances,
    staleTime: 1000 * 60 * 5,
  });
}
