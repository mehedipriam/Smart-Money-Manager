import { useSearchParams } from 'react-router-dom';

/** Reads a single query-string parameter (used by the email-verification and password-reset links). */
export function useQueryParam(name) {
  const [searchParams] = useSearchParams();
  return searchParams.get(name);
}
