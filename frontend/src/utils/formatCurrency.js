export function formatCurrency(amount, currencyCode) {
  const value = Number(amount);
  try {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: currencyCode }).format(value);
  } catch {
    return `${value.toFixed(2)} ${currencyCode}`;
  }
}

export const ACCOUNT_TYPE_LABELS = {
  CASH: 'Cash',
  BANK_ACCOUNT: 'Bank Account',
  MOBILE_BANKING: 'Mobile Banking',
  CREDIT_CARD: 'Credit Card',
  SAVINGS_ACCOUNT: 'Savings Account',
  CUSTOM: 'Custom',
};

export const ACCOUNT_TYPES = Object.keys(ACCOUNT_TYPE_LABELS);

export const CURRENCIES = ['BDT', 'USD', 'EUR', 'GBP'];
