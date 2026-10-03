import API from './axiosInstance';

export const paymentsApi = {
  getInvoiceByBookingId: async (bookingId) => {
    const response = await API.get(`/payments/invoices/booking/${bookingId}`);
    return response.data;
  },

  getInvoiceDetails: async (invoiceId) => {
    const response = await API.get(`/payments/invoices/${invoiceId}`);
    return response.data;
  },

  getCustomerInvoices: async (customerId) => {
    const response = await API.get(`/payments/customer/${customerId}/invoices`);
    return response.data;
  },

  processPayment: async ({ invoiceId, paymentMethodId, amount, idempotencyKey }) => {
    const response = await API.post('/payments/process', {
      invoiceId,
      paymentMethodId,
      amount,
      idempotencyKey,
    });
    return response.data;
  },

  getCustomerMethods: async (customerId) => {
    const response = await API.get(`/payments/customer/${customerId}/methods`);
    return response.data;
  },

  addCustomerPaymentMethod: async (customerId, { cardNumber, simulatedBalance, expiryDate }) => {
    const response = await API.post(`/payments/customer/${customerId}/methods`, {
      cardNumber,
      simulatedBalance,
      expiryDate,
    });
    return response.data;
  },

  placeSecurityHold: async ({ invoiceId, paymentMethodId, holdAmount, idempotencyKey }) => {
    const response = await API.post('/payments/holds/place', null, {
      params: { invoiceId, paymentMethodId, holdAmount, idempotencyKey },
    });
    return response.data;
  },

  releaseSecurityHold: async (paymentId, reason) => {
    const response = await API.post(`/payments/holds/${paymentId}/release`, null, { params: { reason } });
    return response.data;
  },

  getActiveSecurityHold: async (bookingId) => {
    const response = await API.get(`/payments/holds/booking/${bookingId}`);
    return response.data;
  },

  chargeDamage: async ({ invoiceId, paymentMethodId, damageAmount, damageDescription, idempotencyKey, existingSecurityHoldPaymentId }) => {
    const response = await API.post('/payments/damage/charge', {
      invoiceId,
      paymentMethodId,
      damageAmount,
      damageDescription,
      idempotencyKey,
      existingSecurityHoldPaymentId,
    });
    return response.data;
  },

  issueRefund: async (paymentId, { refundAmount, reason }) => {
    const response = await API.post(`/payments/${paymentId}/refund`, {
      refundAmount,
      reason,
    });
    return response.data;
  },

  triggerNoShows: async () => {
    const response = await API.post('/payments/cron/no-shows');
    return response.data;
  },

  configureSimulatorRule: async ({ cardNumberTrigger, simulatedResponseCode, latencyMs }) => {
    const response = await API.post('/payments/simulator/rules', {
      cardNumberTrigger,
      simulatedResponseCode,
      latencyMs,
    });
    return response.data;
  },
};

export default paymentsApi;
