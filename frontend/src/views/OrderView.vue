<template>
  <v-container>
    <v-row>
      <v-col>
        <h2>Orders</h2>
      </v-col>
      <v-col cols="auto">
        <v-btn color="primary" prepend-icon="$plus" @click="openOrderDialog()">
          New Order
        </v-btn>
      </v-col>
    </v-row>

    <!-- Loading state -->
    <div v-if="loading" class="d-flex justify-center align-center my-5">
      <v-progress-circular indeterminate color="primary"></v-progress-circular>
    </div>

    <!-- Error state -->
    <v-alert v-if="error" type="error" class="my-3">
      {{ error }}
      <template v-slot:append>
        <v-btn variant="text" @click="loadOrders">Retry</v-btn>
      </template>
    </v-alert>

    <!-- Orders table -->
    <v-card v-if="!loading && !error">
      <v-data-table
        :headers="tableHeaders"
        :items="orders"
        :items-per-page="10"
        class="elevation-1"
      >
        <!-- Format currency in the totalAmount column -->
        <template v-slot:item.totalAmount="{ item }">
          ${{ Number(item.totalAmount).toFixed(2) }}
        </template>

        <!-- Format dates -->
        <template v-slot:item.orderDate="{ item }">
          {{ formatDate(item.orderDate) }}
        </template>

        <template v-slot:item.dueDate="{ item }">
          {{ formatDate(item.dueDate) }}
        </template>

        <!-- Status with colored chip -->
        <template v-slot:item.status="{ item }">
          <v-chip
            :color="getStatusColor(item.status)"
            size="small"
          >
            {{ item.status }}
          </v-chip>
        </template>

        <!-- Actions column -->
        <template v-slot:item.actions="{ item }">
          <v-icon 
            size="small"
            class="me-2"
            @click="openOrderDialog(item)"
          >
            $pencil
          </v-icon>
          <v-icon 
            size="small"
            color="error"
            @click="confirmDelete(item)"
          >
            $delete
          </v-icon>
        </template>
      </v-data-table>
    </v-card>

    <!-- Order form dialog -->
    <v-dialog v-model="showOrderDialog" max-width="800px">
      <v-card>
        <v-card-title>
          <span class="text-h5">{{ isEditMode ? 'Edit Order' : 'New Order' }}</span>
        </v-card-title>
        
        <v-card-text>
          <v-form ref="form" v-model="formValid" @submit.prevent="saveOrder">
            <v-container>
              <v-row>
                <!-- Customer Information -->
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="currentOrder.customerName"
                    label="Customer Name"
                    required
                    :rules="[v => !!v || 'Name is required']"
                  ></v-text-field>
                </v-col>
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="currentOrder.customerPhone"
                    label="Phone Number"
                    required
                    :rules="[v => !!v || 'Phone number is required']"
                  ></v-text-field>
                </v-col>
                <v-col cols="12">
                  <v-text-field
                    v-model="currentOrder.customerEmail"
                    label="Email"
                    type="email"
                  ></v-text-field>
                </v-col>

                <!-- Order Details -->
                <v-col cols="12">
                  <v-textarea
                    v-model="currentOrder.orderDetails"
                    label="Order Details"
                    rows="3"
                    required
                    :rules="[v => !!v || 'Order details are required']"
                  ></v-textarea>
                </v-col>

                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="currentOrder.totalAmount"
                    label="Total Amount"
                    type="number"
                    prefix="$"
                    required
                    :rules="[
                      v => !!v || 'Amount is required',
                      v => Number(v) > 0 || 'Amount must be greater than 0'
                    ]"
                  ></v-text-field>
                </v-col>

                <!-- Dates -->
                <v-col cols="12" md="6">
                  <v-select
                    v-model="currentOrder.status"
                    label="Status"
                    :items="orderStatuses"
                    required
                  ></v-select>
                </v-col>
                
                <v-col cols="12" md="6">
                  <v-menu
                    v-model="showOrderDatePicker"
                    :close-on-content-click="false"
                  >
                    <template v-slot:activator="{ props }">
                      <v-text-field
                        v-model="formattedOrderDate"
                        label="Order Date"
                        readonly
                        v-bind="props"
                      ></v-text-field>
                    </template>
                    <v-date-picker
                      v-model="currentOrder.orderDate"
                      @update:model-value="showOrderDatePicker = false"
                    ></v-date-picker>
                  </v-menu>
                </v-col>

                <v-col cols="12" md="6">
                  <v-menu
                    v-model="showDueDatePicker"
                    :close-on-content-click="false"
                  >
                    <template v-slot:activator="{ props }">
                      <v-text-field
                        v-model="formattedDueDate"
                        label="Due Date"
                        readonly
                        v-bind="props"
                      ></v-text-field>
                    </template>
                    <v-date-picker
                      v-model="currentOrder.dueDate"
                      @update:model-value="showDueDatePicker = false"
                    ></v-date-picker>
                  </v-menu>
                </v-col>
              </v-row>
            </v-container>
          </v-form>
        </v-card-text>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="error" variant="text" @click="showOrderDialog = false">Cancel</v-btn>
          <v-btn 
            color="primary" 
            variant="text" 
            @click="saveOrder"
            :disabled="!formValid"
            :loading="saving"
          >Save</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Delete confirmation dialog -->
    <v-dialog v-model="showDeleteDialog" max-width="400px">
      <v-card>
        <v-card-title class="text-h5">Confirm Delete</v-card-title>
        <v-card-text>
          Are you sure you want to delete this order for {{ orderToDelete?.customerName }}?
          This action cannot be undone.
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" variant="text" @click="showDeleteDialog = false">Cancel</v-btn>
          <v-btn 
            color="error" 
            variant="text" 
            @click="deleteOrder"
            :loading="deleting"
          >Delete</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { OrderAPI, type Order } from '../services/api';
import { globalNotifications } from '../composables/notifications';

// Table headers
const tableHeaders = [
  { title: 'Customer', key: 'customerName' },
  { title: 'Phone', key: 'customerPhone' },
  { title: 'Total', key: 'totalAmount' },
  { title: 'Order Date', key: 'orderDate' },
  { title: 'Due Date', key: 'dueDate' },
  { title: 'Status', key: 'status' },
  { title: 'Actions', key: 'actions', sortable: false }
];

// Order status options
const orderStatuses = ['Pending', 'Confirmed', 'In Progress', 'Ready', 'Delivered', 'Cancelled'];

// State
const orders = ref<Order[]>([]);
const loading = ref(false);
const saving = ref(false);
const deleting = ref(false);
const error = ref('');
const form = ref();
const formValid = ref(false);

// Dialog controls
const showOrderDialog = ref(false);
const showDeleteDialog = ref(false);
const showOrderDatePicker = ref(false);
const showDueDatePicker = ref(false);

// Current order being edited or created
const currentOrder = ref<Order>({
  customerName: '',
  customerPhone: '',
  customerEmail: '',
  orderDetails: '',
  totalAmount: 0,
  orderDate: new Date().toISOString().substr(0, 10),
  dueDate: new Date().toISOString().substr(0, 10),
  status: 'Pending'
});

// Order to delete
const orderToDelete = ref<Order | null>(null);

// Edit mode flag
const isEditMode = ref(false);

// Computed properties for formatted dates
const formattedOrderDate = computed(() => {
  return formatDate(currentOrder.value.orderDate);
});

const formattedDueDate = computed(() => {
  return formatDate(currentOrder.value.dueDate);
});

// Load orders when component is mounted
onMounted(async () => {
  await loadOrders();
});

// Load all orders from the API
async function loadOrders() {
  loading.value = true;
  error.value = '';
  
  try {
    orders.value = await OrderAPI.getAllOrders();
  } catch (err) {
    console.error('Failed to load orders:', err);
    error.value = 'Failed to load orders. Please try again.';
  } finally {
    loading.value = false;
  }
}

// Open the order dialog for create or edit
function openOrderDialog(order?: Order) {
  if (order) {
    // Edit mode - clone the order to avoid directly modifying the table data
    const clonedOrder = { ...order };
    
    // Convert dates from backend format to date picker format (YYYY-MM-DD)
    if (clonedOrder.orderDate) {
      clonedOrder.orderDate = clonedOrder.orderDate.split('T')[0];
    }
    if (clonedOrder.dueDate) {
      clonedOrder.dueDate = clonedOrder.dueDate.split('T')[0];
    }
    
    currentOrder.value = clonedOrder;
    isEditMode.value = true;
  } else {
    // Create mode - reset to defaults
    currentOrder.value = {
      customerName: '',
      customerPhone: '',
      customerEmail: '',
      orderDetails: '',
      totalAmount: 0,
      orderDate: new Date().toISOString().substr(0, 10),
      dueDate: new Date().toISOString().substr(0, 10),
      status: 'Pending'
    };
    isEditMode.value = false;
  }
  
  showOrderDialog.value = true;
}

// Save the current order (create or update)
async function saveOrder() {
  if (!formValid.value) return;
  
  saving.value = true;
  
  try {
    // Create a copy of the current order with properly formatted dates
    const orderToSave = {
      ...currentOrder.value,
      // Convert ISO date strings to LocalDateTime format for backend
      orderDate: `${currentOrder.value.orderDate}T00:00:00`,
      dueDate: `${currentOrder.value.dueDate}T00:00:00`
    };
    
    if (isEditMode.value && currentOrder.value.id) {
      // Update existing order
      const updatedOrder = await OrderAPI.updateOrder(
        currentOrder.value.id,
        orderToSave
      );
      
      // Update the order in the table
      const index = orders.value.findIndex(o => o.id === updatedOrder.id);
      if (index !== -1) {
        orders.value[index] = updatedOrder;
      }
    } else {
      // Create new order
      const newOrder = await OrderAPI.createOrder(orderToSave);
      orders.value.push(newOrder);
    }
    
    // Close dialog on success
    showOrderDialog.value = false;
    
    // Refresh orders list to ensure we have latest data
    await loadOrders();
    
    // Show success notification
    globalNotifications.success(isEditMode.value ? 'Order updated successfully' : 'New order created successfully');
  } catch (err) {
    console.error('Failed to save order:', err);
    globalNotifications.error('Failed to save order. Please try again.');
  } finally {
    saving.value = false;
  }
}

// Confirm order deletion
function confirmDelete(order: Order) {
  orderToDelete.value = order;
  showDeleteDialog.value = true;
}

// Delete the selected order
async function deleteOrder() {
  if (!orderToDelete.value?.id) return;
  
  deleting.value = true;
  
  try {
    await OrderAPI.deleteOrder(orderToDelete.value.id);
    
    // Remove from the table
    orders.value = orders.value.filter(o => o.id !== orderToDelete.value?.id);
    
    // Close dialog on success
    showDeleteDialog.value = false;
    orderToDelete.value = null;
    
    // Show success notification
    globalNotifications.success('Order deleted successfully');
  } catch (err) {
    console.error('Failed to delete order:', err);
    globalNotifications.error('Failed to delete order. Please try again.');
  } finally {
    deleting.value = false;
  }
}

// Format date for display
function formatDate(dateString: string): string {
  if (!dateString) return '';
  
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return dateString;
  
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  }).format(date);
}

// Get color for status chip
function getStatusColor(status: string): string {
  switch (status) {
    case 'Pending': return 'blue';
    case 'Confirmed': return 'purple';
    case 'In Progress': return 'orange';
    case 'Ready': return 'indigo';
    case 'Delivered': return 'success';
    case 'Cancelled': return 'error';
    default: return 'grey';
  }
}
</script>

<style scoped>
.v-data-table {
  margin-top: 16px;
}
</style>