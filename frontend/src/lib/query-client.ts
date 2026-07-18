import { QueryClient } from "@tanstack/react-query";
import { ApiRequestError } from "./api-helpers";

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: (failureCount, error) => {
        // Don't retry auth/permission errors — they won't resolve by retrying.
        if (error instanceof ApiRequestError && error.status && [400, 401, 403, 404].includes(error.status)) {
          return false;
        }
        return failureCount < 2;
      },
      refetchOnWindowFocus: false,
    },
    mutations: {
      retry: false,
    },
  },
});
