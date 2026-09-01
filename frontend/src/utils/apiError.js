/** Pulls the standard ApiResponse error shape ({ message, errors }) out of a failed axios call. */
export function getErrorMessage(error, fallback = 'Something went wrong. Please try again.') {
  return error?.response?.data?.message || fallback;
}

export function getFieldErrors(error) {
  return error?.response?.data?.errors || {};
}
