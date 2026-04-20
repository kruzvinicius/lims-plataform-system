import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../api/axiosClient';

// Domain entity strict types reflecting the LIMS Java Backend
export type SampleStatus = 'PENDING_RECEIPT' | 'RECEIVED' | 'IN_ANALYSIS' | 'PENDING_APPROVAL' | 'APPROVED' | 'RELEASED' | 'REJECTED' | 'REANALYSIS_REQUESTED' | 'IN_REANALYSIS';
export type PriorityLevel = 'URGENT' | 'ROUTINE';

export interface Sample {
  id: number;
  barcode: string;
  patientName: string;
  status: SampleStatus;
  priority: PriorityLevel;
  collectionDate: string;
  testType: string;
}

export const fetchSamples = async (): Promise<Sample[]> => {
  const { data } = await apiClient.get('/samples'); 
  // Spring Boot Pagination API returns the array inside 'content'
  return data.content || [];
};

// Custom Hook to abstract caching, retries, and background synchronization
export function useSamples() {
  return useQuery({
    queryKey: ['samples_live_table'],
    queryFn: fetchSamples,
    staleTime: 1000 * 60 * 5, // Keep cached data fresh without network calls for 5 minutes
    refetchOnWindowFocus: true, // Automatically re-sync if the lab technician switches tabs
  });
}
