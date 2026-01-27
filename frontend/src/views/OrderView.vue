<template>
  <v-container>
    <v-row>
      <v-col class="d-flex justify-space-between align-center">
        <h2>Orders</h2>
        <v-btn icon="$filter" variant="text" density="comfortable" @click="openFilterDialog"></v-btn>
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

    <!-- Orders card list -->
    <v-container v-if="!loading && !error" class="orders-grid-container px-0">
      <v-row class="orders-grid">
        <v-col
          cols="12"
          sm="6"
          md="4"
          v-for="order in orders"
          :key="order.id"
          class="mb-4"
        >
          <v-card
            class="order-card"
            elevation="2"
            @click="openOrderDetails(order)"
          >
            <v-card-title class="d-flex justify-space-between">
              <div style="white-space: pre-wrap;">
                {{ formatDate(order.pickupDate) }} - {{ order.orderSummary }}
              </div>
              <v-btn
                icon="$edit"
                density="compact"
                variant="text"
                color="primary"
                size="small"
                @click.stop="openOrderDialog(order)"
                class="ms-2"
              ></v-btn>
            </v-card-title>
            <v-card-text class="d-flex flex-column">
              <v-slide-group show-arrows>
                <v-slide-group-item v-for="image in order.images" :key="image.path">
                    <img :src="image.url" alt="preview" class="mb-2" height="50" width="50"
                      style="object-fit: cover;" />
                </v-slide-group-item>
              </v-slide-group>
              <div class="order-details mb-2">{{ order.orderDetails }}</div>
              <div class="d-flex justify-space-between align-center mt-auto">
                <div><strong>${{ Number(order.totalAmount || 0).toFixed(2) }}</strong></div>
                <div>{{ order.isDelivery ? 'Delivery' : 'Pickup' }}</div>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>

    <!-- Order form dialog -->
    <v-dialog v-model="showOrderDialog" max-width="800px">
      <v-card>
        <v-card-title class="px-8 pb-0 d-flex justify-space-between align-center">
          {{ isEditMode ? 'Edit Order' : 'New Order' }}
          <v-btn icon="$close" variant="text" size="small" @click="showOrderDialog = false" class="ms-2"></v-btn>
        </v-card-title>
        
        <v-card-text class="px-4 pt-0">
          <v-form ref="form" v-model="formValid" @submit.prevent="saveOrder">
            <v-container>
              <v-row dense>
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
                    :rules="[
                      v => !v || /^[\d\s\-\+\(\)]+$/.test(v) || 'Invalid phone number format',
                      v => !v || v.replace(/\D/g, '').length >= 10 || 'Phone number must have at least 10 digits'
                    ]"
                  ></v-text-field>
                </v-col>
                <v-col cols="12">
                  <v-text-field
                    v-model="currentOrder.customerEmail"
                    label="Email"
                    type="email"
                    autocomplete="email"
                    :rules="[
                      v => !v || /.+@.+\..+/.test(v) || 'Invalid email format'
                    ]"
                  ></v-text-field>
                </v-col>

                <!-- Order Summary and Details -->
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="currentOrder.orderSummary"
                    label="Order Summary"
                    required
                    :rules="[v => !!v || 'Order summary is required']"
                  ></v-text-field>
                </v-col>

                <v-col cols="12">
                  <v-textarea
                    v-model="currentOrder.orderDetails"
                    label="Order Details"
                    rows="8"
                  ></v-textarea>
                </v-col>

                <v-col cols="12" md="12">
                  <v-file-input
                    ref="fileInputRef"
                    prepend-icon=""
                    prepend-inner-icon="$upload"
                    label="Upload Images"
                    :multiple="true"
                    v-model="newFiles"
                    @update:model-value="handleFileChange"
                    filter-by-type="image/*"
                    accept="image/*"
                  ></v-file-input>
                </v-col>
                
                <v-col cols="12" md="12">
                  <v-sheet color="grey-lighten-4" elevation="1">
                    <v-slide-group show-arrows>
                      <v-slide-group-item v-for="(uploadedFile, index) in uploadedFiles" :key="index" v-slot="{ isSelected, toggle }">
                        <v-card :color="isSelected ? 'primary' : 'grey-lighten-1'" class="ma-4" height="100" width="100"
                          @click="toggle">
                          <img :src="uploadedFile.url" alt="preview" style="width: 100%; height: 100%; object-fit: cover;"/>
                          <div v-if="isSelected"  class="d-flex fill-height align-center justify-center" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); cursor: pointer;">
                            <v-scale-transition>
                              <v-icon color="white" icon="$delete"
                                size="24" @click="removeImage(index)"></v-icon>
                            </v-scale-transition>
                          </div>
                        </v-card>
                      </v-slide-group-item>
                    </v-slide-group>
                  </v-sheet>
                </v-col>

                <!-- Financial Information -->
                <v-col cols="12" md="4">
                  <v-text-field
                    v-model="currentOrder.totalAmount"
                    label="Total Amount"
                    type="number"
                    prefix="$"
                    required
                    :rules="[
                      v => !!v || 'Amount is required',
                      v => Number(v) > 0 || 'Amount must be greater than 0',
                      v => Number(v) <= 10000 || 'Amount cannot be greater than 10000'
                    ]"
                  ></v-text-field>
                </v-col>

                <v-col cols="12" md="4">
                  <v-text-field
                    v-model="currentOrder.deposit"
                    label="Deposit"
                    type="number"
                    prefix="$"
                    :rules="[
                    v => Number(v) >= 0 || 'Amount must be greater or equal to 0',
                    v => Number(v) <= 10000 || 'Amount cannot be greater than 10000'
                  ]"
                  ></v-text-field>
                </v-col>
                
                <!-- Delivery/Pickup Options -->
                <v-col cols="12" md="4">
                  <v-switch
                    v-model="currentOrder.isDelivery"
                    label="Delivery"
                  ></v-switch>
                </v-col>

                <v-col cols="12" md="4" v-if="currentOrder.isDelivery">
                  <v-text-field
                    v-model="currentOrder.deliveryFee"
                    label="Delivery Fee"
                    type="number"
                    prefix="$"
                  ></v-text-field>
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
                    v-model="showPickupDatePicker"
                    :close-on-content-click="false"
                  >
                    <template v-slot:activator="{ props }">
                      <v-text-field
                        v-model="formattedPickupDate"
                        :label="currentOrder.isDelivery ? 'Delivery Date' : 'Pickup Date'"
                        readonly
                        v-bind="props"
                      ></v-text-field>
                    </template>
                    <v-date-picker
                      v-model="currentOrder.pickupDate"
                      @update:model-value="showPickupDatePicker = false"
                    ></v-date-picker>
                  </v-menu>
                </v-col>
                <v-col cols="12" md="6">
                  <v-select
                    v-model="currentOrder.status"
                    label="Status"
                    :items="orderStatuses"
                    required
                  ></v-select>
                </v-col>
              </v-row>
            </v-container>
          </v-form>
        </v-card-text>

        <v-card-actions>
          <v-btn 
            v-if="isEditMode" 
            color="error" 
            variant="text" 
            prepend-icon="$delete" 
            @click="confirmDelete(currentOrder)"
          >Delete</v-btn>
          <v-spacer></v-spacer>
          <v-btn color="grey-darken-1" variant="text" @click="showOrderDialog = false">Cancel</v-btn>
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

    <!-- Order details dialog -->
    <v-dialog v-model="showOrderDetailsDialog" max-width="700px">
      <v-card v-if="selectedOrder" class="order-details-card">
        <!-- Header with order summary and status -->
        <v-card-item>
          <template v-slot:prepend>
            <v-avatar color="primary" class="text-white" size="48">
              {{ selectedOrder.customerName.charAt(0).toUpperCase() }}
            </v-avatar>
          </template>
          
          <v-card-title class="text-h5 ps-2">
            {{ selectedOrder.orderSummary }}
          </v-card-title>
          
          <template v-slot:append>
            <v-chip
              :color="getStatusColor(selectedOrder.status)"
              :text="selectedOrder.status"
              size="small"
              class="ms-2"
            ></v-chip>
            <v-btn
              icon="$close"
              variant="text"
              size="small"
              @click="showOrderDetailsDialog = false"
              class="ms-2"
            ></v-btn>
          </template>
        </v-card-item>

        <v-divider></v-divider>
        
        <v-card-text class="pt-4">
          <!-- Order details section -->
          <h3 class="text-h6 mb-3">Order Details</h3>
          <v-card flat border class="mb-4 pa-3 order-details-text">
            <p style="white-space: pre-line">{{ selectedOrder.orderDetails }}</p>
          </v-card>

          <v-slide-group show-arrows>
            <v-slide-group-item v-for="image in selectedOrder.images" :key="image.path">
              <img :src="image.url" alt="preview" class="mb-2" height="200" width="200" style="object-fit: cover;" />
            </v-slide-group-item>
          </v-slide-group>

          <v-divider class="my-4"></v-divider>

          <!-- Financial summary -->
          <!-- <h3 class="text-h6 mb-3">Financial Summary</h3> -->
          <v-row class="financial-summary">
            <v-col cols="6" sm="3" class="financial-item">
              <v-card flat border height="100%">
                <v-card-text class="text-center">
                  <div class="text-subtitle-2">Total Amount</div>
                  <div class="text-h5 mt-2">${{ Number(selectedOrder.totalAmount || 0).toFixed(2) }}</div>
                </v-card-text>
              </v-card>
            </v-col>

            <v-col cols="6" sm="3" v-if="selectedOrder.deposit && Number(selectedOrder.deposit) > 0"
              class="financial-item">
              <v-card flat border height="100%">
                <v-card-text class="text-center">
                  <div class="text-subtitle-2">Deposit</div>
                  <div class="text-h5 mt-2">${{ Number(selectedOrder.deposit).toFixed(2) }}</div>
                </v-card-text>
              </v-card>
            </v-col>

            <v-col cols="6" sm="3" v-if="selectedOrder.tip && Number(selectedOrder.tip) > 0" class="financial-item">
              <v-card flat border height="100%">
                <v-card-text class="text-center">
                  <div class="text-subtitle-2">Tip</div>
                  <div class="text-h5 mt-2">${{ Number(selectedOrder.tip).toFixed(2) }}</div>
                </v-card-text>
              </v-card>
            </v-col>

            <v-col cols="6" sm="3"
              v-if="selectedOrder.isDelivery && selectedOrder.deliveryFee && Number(selectedOrder.deliveryFee) > 0"
              class="financial-item">
              <v-card flat border height="100%">
                <v-card-text class="text-center">
                  <div class="text-subtitle-2">Delivery Fee</div>
                  <div class="text-h5 mt-2">${{ Number(selectedOrder.deliveryFee).toFixed(2) }}</div>
                </v-card-text>
              </v-card>
            </v-col>
          </v-row>


          <!-- Order info section with key details -->
          <v-row>
            <!-- Customer information -->
            <v-col cols="12" sm="6">
              <v-list density="compact" bg-color="transparent">
                <v-list-subheader class="ps-0 text-primary">Customer Information</v-list-subheader>
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon icon="$account" color="primary" size="small"></v-icon>
                  </template>
                  <v-list-item-title>{{ selectedOrder.customerName }}</v-list-item-title>
                </v-list-item>
                
                <v-list-item v-if="selectedOrder.customerPhone">
                  <template v-slot:prepend>
                    <v-icon icon="$phone" color="primary" size="small"></v-icon>
                  </template>
                  <v-list-item-title>{{ selectedOrder.customerPhone }}</v-list-item-title>
                </v-list-item>
                
                <v-list-item v-if="selectedOrder.customerEmail">
                  <template v-slot:prepend>
                    <v-icon icon="$email" color="primary" size="small"></v-icon>
                  </template>
                  <v-list-item-title>{{ selectedOrder.customerEmail }}</v-list-item-title>
                </v-list-item>
              </v-list>
            </v-col>
            
            <!-- Order dates -->
            <v-col cols="12" sm="6">
              <v-list density="compact" bg-color="transparent">
                <v-list-subheader class="ps-0 text-primary">Order Dates</v-list-subheader>
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon icon="$calendar" color="primary" size="small"></v-icon>
                  </template>
                  <v-list-item-title>Order Date: {{ formatDate(selectedOrder.orderDate, true) }}</v-list-item-title>
                </v-list-item>
                
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon :icon="selectedOrder.isDelivery ? '$truck' : '$store'" color="primary" size="small"></v-icon>
                  </template>
                  <v-list-item-title>
                    {{ selectedOrder.isDelivery ? 'Delivery' : 'Pickup' }} Date: {{ formatDate(selectedOrder.pickupDate, true) }}
                  </v-list-item-title>
                </v-list-item>
              </v-list>
            </v-col>
          </v-row>
        </v-card-text>
        
        <v-divider></v-divider>
        
        <!-- Action buttons -->
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey-darken-1" variant="text" @click="showOrderDetailsDialog = false">Close</v-btn>
          <v-btn color="primary" variant="text" @click="openOrderDialog(selectedOrder)">Edit</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    
    <!-- Floating Action Button for adding new orders -->
    <v-btn
      color="primary"
      icon="$plus"
      size="large"
      class="fab-button"
      @click="openOrderDialog()"
      elevation="4"
    ></v-btn>

    <!-- Filter Dialog -->
    <v-dialog v-model="showFilterDialog" max-width="500px">
      <v-card>
        <v-card-title class="px-8 pb-0 mt-2 position-relative d-flex justify-center align-center">
          Filter
          <v-btn icon="$close" variant="text" size="small" @click="showFilterDialog = false" class="position-absolute" style="right: 10px;"></v-btn>
        </v-card-title>
        
        <v-card-text class="px-4 pt-2">
          <v-container>
            <v-row dense>
              <v-col cols="12">
                <h3 class="text-subtitle-1 mb-2">Date Range</h3>
              </v-col>
              <v-col cols="6">
                <v-menu
                  v-model="showFromDatePicker"
                  :close-on-content-click="false"
                >
                  <template v-slot:activator="{ props }">
                    <v-text-field
                      v-model="formattedFromDate"
                      label="From Date"
                      readonly
                      v-bind="props"
                    ></v-text-field>
                  </template>
                  <v-date-picker
                    v-model="filterFromDate"
                    @update:model-value="showFromDatePicker = false"
                  ></v-date-picker>
                </v-menu>
              </v-col>
              <v-col cols="6">
                <v-menu
                  v-model="showToDatePicker"
                  :close-on-content-click="false"
                >
                  <template v-slot:activator="{ props }">
                    <v-text-field
                      v-model="formattedToDate"
                      label="To Date"
                      readonly
                      v-bind="props"
                    ></v-text-field>
                  </template>
                  <v-date-picker
                    v-model="filterToDate"
                    @update:model-value="showToDatePicker = false"
                  ></v-date-picker>
                </v-menu>
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey-darken-1" variant="text" @click="resetFilter">Reset</v-btn>
          <v-btn color="grey-darken-1" variant="text" @click="showFilterDialog = false">Cancel</v-btn>
          <v-btn color="primary" variant="text" @click="applyFilter">Apply</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { OrderAPI, type Order } from '../services/order-api';
import { FileAPI, type SignedUrlReq } from '../services/file-api';
import { globalNotifications } from '../composables/notifications';

// Order status options
const orderStatuses = ['Confirmed', 'Picked up', 'Delivered', 'Cancelled'];

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
const showPickupDatePicker = ref(false);
const showOrderDetailsDialog = ref(false);
const showFilterDialog = ref(false);
const showFromDatePicker = ref(false);
const showToDatePicker = ref(false);
const selectedOrder = ref<Order | null>(null);

// Date range filter state - default from today to 90 days from now
const today = new Date();
const futureDate = new Date(today.getTime() + 90 * 24 * 60 * 60 * 1000); // 3 months from now
const filterFromDate = ref<string>(today.toISOString());
const filterToDate = ref<string>(futureDate.toISOString());

// Current order being edited or created
const currentOrder = ref<Order>({
  customerName: '',
  customerPhone: '',
  customerEmail: '',
  orderSummary: '',
  orderDetails: '',
  totalAmount: null as unknown as number,
  deposit: null as unknown as number,
  tip: 0,
  isDelivery: false,
  deliveryFee: 0,
  pickupDate: new Date().toISOString(),
  orderDate: new Date().toISOString(),
  status: 'Confirmed',
  images: []
});

interface UploadedFile {
  url: string;
  path: string;
}

const newFiles = ref(<File[]>[]);
const uploadedFiles = ref<UploadedFile[]>([]);
const fileInputRef = ref();

// Order to delete
const orderToDelete = ref<Order | null>(null);

// Edit mode flag
const isEditMode = ref(false);

// Computed properties for formatted dates
const formattedOrderDate = computed(() => {
  return formatDate(currentOrder.value.orderDate, true);
});

const formattedPickupDate = computed(() => {
  return formatDate(currentOrder.value.pickupDate, true);
});

const formattedFromDate = computed(() => {
  return formatDate(filterFromDate.value, true);
});

const formattedToDate = computed(() => {
  return formatDate(filterToDate.value, true);
});

// Function to open order details
function openOrderDetails(order: Order) {
  selectedOrder.value = order;
  showOrderDetailsDialog.value = true;
}

function openFilterDialog() {
  showFilterDialog.value = true;
}

async function applyFilter() {
  if (new Date(filterFromDate.value) > new Date(filterToDate.value)) {
    globalNotifications.error('From date must be before to date');
    return;
  }
  
  showFilterDialog.value = false;
  await loadOrders(filterFromDate.value, filterToDate.value);
}

function resetFilter() {
  const today = new Date();
  const futureDate = new Date(today.getTime() + 90 * 24 * 60 * 60 * 1000); // 3 months from now
  filterFromDate.value = today.toISOString();
  filterToDate.value = futureDate.toISOString();
}

// Load orders when component is mounted
onMounted(async () => {
  await loadOrders();
});

async function loadOrders(fromDate?: string, toDate?: string) {
  loading.value = true;
  error.value = '';
  
  try {
    const from = fromDate ? new Date(fromDate).toISOString() : undefined;
    const to = toDate ? new Date(toDate).toISOString() : undefined;
    orders.value = await OrderAPI.getAllOrders(from, to);
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
    currentOrder.value = clonedOrder;
    isEditMode.value = true;
    uploadedFiles.value = order.images;
  } else {
    // Create mode - reset to defaults
    currentOrder.value = {
      customerName: '',
      customerPhone: '',
      customerEmail: '',
      orderSummary: '',
      orderDetails: '',
      totalAmount: null as unknown as number,
      deposit: null as unknown as number,
      tip: 0,
      isDelivery: false,
      deliveryFee: 0,
      pickupDate: new Date().toISOString(),
      orderDate: new Date().toISOString(),
      status: 'Confirmed',
      images: []
    };
    isEditMode.value = false;
    uploadedFiles.value = [];
  }
  newFiles.value = [];
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
      // Convert ISO date strings for backend
      orderDate: new Date(currentOrder.value.orderDate).toISOString(),
      pickupDate: new Date(currentOrder.value.pickupDate).toISOString(),
      // Convert null values to zero for backend compatibility
      totalAmount: currentOrder.value.totalAmount || 0,
      deposit: currentOrder.value.deposit || 0,
      images: uploadedFiles.value
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
    showOrderDetailsDialog.value = false;
    uploadedFiles.value = [];
    
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
function formatDate(dateString: string, withYear: boolean = false): string {
  if (!dateString) return '';
  
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return dateString;
  
  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    ...(withYear && { year: 'numeric' })
  }).format(date);
}

// Get color for status chip
function getStatusColor(status: string): string {
  switch (status) {
    case 'Confirmed': return 'info';
    case 'Picked up': return 'success';
    case 'Delivered': return 'success';
    case 'Cancelled': return 'error';
    default: return 'grey';
  }
}

async function handleFileChange(files: File | File[]) {
  const curFiles = Array.isArray(files) ? files : [files];
  const signedUrls = await FileAPI.getSignedUrl(curFiles.map(file => ({
    fileName: file.name,
    fileType: file.type,
    fileSize: file.size / 1024 / 1024 // Convert to MB
  })));
  curFiles.forEach(file => {
    const filteredSignedUrls = signedUrls.filter(signedUrl => {
      return signedUrl.fileName === file.name;
    })

    fetch(filteredSignedUrls[0].signedUrl, {
      method: 'PUT',
      headers: {
        'Content-Type': file.type
      },
      body: file
    });

    uploadedFiles.value.push({
      url: URL.createObjectURL(file),
      path: filteredSignedUrls[0].tempPath
    })
  })
  newFiles.value = [];
  fileInputRef.value.blur();
}

function removeImage(index: number) {
  uploadedFiles.value.splice(index, 1);
}

</script>

<style scoped>
.order-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  height: 100%;
  width: 100%;  /* Ensure card takes full width of its column */
  display: flex;
  flex-direction: column;
}

.order-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1) !important;
}

.order-details {
  white-space: pre-line;
  word-break: break-word;
}

.orders-grid-container {
  width: 100%;
}

.orders-grid {
  display: flex;
  flex-wrap: wrap;
}

/* Ensure v-col elements have equal width within their breakpoints */
.v-col {
  display: flex;
}

/* Make sure the card takes up the full space in its column */
.v-col > .v-card {
  width: 100%;
}

/* Floating Action Button styles */
.fab-button {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 5;
  border-radius: 50%;
}

/* Order Details Dialog Styles */
.order-details-card .v-card-item {
  padding-bottom: 16px;
}

.order-details-text {
  background-color: rgba(0, 0, 0, 0.02);
  border-radius: 8px;
  min-height: 100px;
}

.financial-summary {
  margin-top: 8px;
}

.financial-item .v-card {
  transition: transform 0.2s;
}

.financial-item .v-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1) !important;
}
</style>