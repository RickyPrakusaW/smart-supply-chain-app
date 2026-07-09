const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const jwt = require('jsonwebtoken');
const crypto = require('crypto');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 5000;
const JWT_SECRET = process.env.JWT_SECRET || 'agrimitra_jwt_super_secret_key';

// Midtrans Client Setup
const midtransClient = require('midtrans-client');
const snap = new midtransClient.Snap({
  isProduction: false,
  serverKey: process.env.MIDTRANS_SERVER_KEY || 'SB-Mid-server-yU5bZ86wD-Mmx1Rj0S9423X7',
  clientKey: process.env.MIDTRANS_CLIENT_KEY || 'SB-Mid-client-W2Q1eW9wX-yU1Rj0'
});

// Middleware
app.use(cors());
app.use(express.json());

// MongoDB Connection
const MONGO_URI = process.env.MONGO_URI;
if (MONGO_URI) {
  mongoose.connect(MONGO_URI)
    .then(() => {
      console.log('Connected to MongoDB Atlas successfully.');
      seedDefaultData();
    })
    .catch(err => console.error('MongoDB Connection Error:', err));
} else {
  console.warn('WARNING: MONGO_URI env variable is not set. Running in-memory/mock mode.');
}

// Schemas & Models
const UserSchema = new mongoose.Schema({
  id: { type: String, required: true, unique: true },
  name: { type: String, required: true },
  email: { type: String, default: null },
  phone: { type: String, default: null },
  role: { type: String, default: 'Pembeli' },
  photoUrl: { type: String, default: null },
  address: { type: String, default: null }
});
const User = mongoose.model('User', UserSchema);

const ProductSchema = new mongoose.Schema({
  id: Number,
  name: String,
  location: String,
  rating: String,
  price: Number,
  quantity: String,
  imageRes: Number,
  category: String,
  isEcoFriendly: { type: Boolean, default: false },
  deliveryDays: Number,
  protein: String,
  fat: String,
  carbs: String,
  calories: String,
  ingredients: String
});
const Product = mongoose.model('Product', ProductSchema);

const FarmerSchema = new mongoose.Schema({
  id: Number,
  name: String,
  rating: String,
  distance: String,
  commodities: String,
  imageRes: Number,
  location: String,
  description: String,
  certs: [String]
});
const Farmer = mongoose.model('Farmer', FarmerSchema);

const OrderSchema = new mongoose.Schema({
  orderId: { type: String, required: true, unique: true },
  userId: { type: String, required: true },
  amount: { type: Number, required: true },
  status: { type: String, default: 'pending' }, // pending, success, failed, expired, cancel
  snapToken: { type: String, default: null },
  redirectUrl: { type: String, default: null },
  items: [
    {
      id: Number,
      name: String,
      price: Number,
      quantity: Number
    }
  ],
  createdAt: { type: Date, default: Date.now }
});
const Order = mongoose.model('Order', OrderSchema);

// In-Memory Database Fallbacks
let inMemoryUsers = [];
let inMemoryOrders = [];
let inMemoryProducts = [
  { id: 1, name: "Telur Ayam Kampung Segar", location: "Peternakan Tani Jaya, Malang", rating: "5.0", price: 24000, quantity: "10 pcs", imageRes: 2131231015, category: "Telur", isEcoFriendly: true, deliveryDays: 1, protein: "13g", fat: "11g", carbs: "1.1g", calories: "155 Kcal", ingredients: "Telur ayam kampung organik segar hasil pakan jagung alami bebas antibiotik." },
  { id: 2, name: "Keju Kambing Organik", location: "Koperasi Susu Pujon, Batu", rating: "4.9", price: 45000, quantity: "200 g", imageRes: 2131231014, category: "Susu", isEcoFriendly: false, deliveryDays: 2, protein: "22g", fat: "24g", carbs: "3g", calories: "360 Kcal", ingredients: "Keju artisan semi-hard buatan tangan dari 100% susu kambing murni berkualitas tinggi." },
  { id: 3, name: "Bayam Hidroponik Bersih", location: "Agro Makmur, Batu", rating: "4.8", price: 12000, quantity: "250 g", imageRes: 2131231016, category: "Sayuran", isEcoFriendly: true, deliveryDays: 1, protein: "2.9g", fat: "0.4g", carbs: "3.6g", calories: "23 Kcal", ingredients: "Sayur bayam hijau segar hidroponik bebas pestisida kimia. Dikemas steril." }
];
let inMemoryFarmers = [
  { id: 1, name: "Koperasi Susu & Keju Pujon", rating: "5.0", distance: "12 km", commodities: "Keju, Susu, Mentega", imageRes: 2131231014, location: "Pujon, Malang", description: "Koperasi susu terpercaya di wilayah Pujon. Kami mengelola ratusan sapi perah lokal secara berkelanjutan.", certs: ["Sertifikasi Organik Kementan", "Sertifikasi Halal MUI"] },
  { id: 2, name: "Agro Makmur Sayur & Buah", rating: "4.8", distance: "15 km", commodities: "Sayur, Tomat, Wortel", imageRes: 2131231016, location: "Batu, Malang", description: "Pertanian hidroponik modern yang menyuplai berbagai sayur segar organik.", certs: ["Sertifikasi Organik Kementan"] }
];

// Helper to seed Mongo DB if empty
async function seedDefaultData() {
  try {
    const productCount = await Product.countDocuments();
    if (productCount === 0) {
      await Product.insertMany(inMemoryProducts);
      console.log('Seeded default products into MongoDB.');
    }
    const farmerCount = await Farmer.countDocuments();
    if (farmerCount === 0) {
      await Farmer.insertMany(inMemoryFarmers);
      console.log('Seeded default farmers into MongoDB.');
    }
  } catch (err) {
    console.error('Failed to seed MongoDB default data:', err);
  }
}

// ---------------- API ENDPOINTS ----------------

// Root Landing Healthcheck
app.get('/', (req, res) => {
  res.json({
    status: "success",
    message: "AgriMitra API is running!",
    database: mongoose.connection.readyState === 1 ? "Connected to MongoDB" : "Running locally (in-memory mode)",
    timestamp: new Date()
  });
});

// Auth Google Sign-in
app.post('/api/v1/auth/google', async (req, res) => {
  const { idToken, name, email } = req.body;
  if (!idToken || !email) {
    return res.status(400).json({ success: false, message: "Missing required fields" });
  }

  const userId = `google_${email.replace(/[^a-zA-Z0-9]/g, "")}`;
  const token = jwt.sign({ id: userId, email }, JWT_SECRET, { expiresIn: '7d' });

  let userObj;
  if (mongoose.connection.readyState === 1) {
    let existing = await User.findOne({ email });
    if (!existing) {
      existing = new User({ id: userId, name, email, role: 'Pembeli' });
      await existing.save();
    }
    userObj = existing.toObject();
  } else {
    // In-memory fallback
    let existing = inMemoryUsers.find(u => u.email === email);
    if (!existing) {
      existing = { id: userId, name, email, phone: null, role: 'Pembeli', photoUrl: null };
      inMemoryUsers.push(existing);
    }
    userObj = existing;
  }

  res.json({
    success: true,
    token: token,
    id: userObj.id,
    name: userObj.name,
    email: userObj.email,
    phone: userObj.phone,
    role: userObj.role,
    photoUrl: userObj.photoUrl,
    address: userObj.address
  });
});

// Auth Phone Sign-in
app.post('/api/v1/auth/phone', async (req, res) => {
  const { phone, name } = req.body;
  if (!phone) {
    return res.status(400).json({ success: false, message: "Phone number is required" });
  }

  const userId = `phone_${phone.replace(/[^0-9]/g, "")}`;
  const token = jwt.sign({ id: userId, phone }, JWT_SECRET, { expiresIn: '7d' });

  let userObj;
  if (mongoose.connection.readyState === 1) {
    let existing = await User.findOne({ phone });
    if (!existing) {
      existing = new User({ id: userId, name: name || "User Telepon", phone, role: 'Pembeli' });
      await existing.save();
    }
    userObj = existing.toObject();
  } else {
    let existing = inMemoryUsers.find(u => u.phone === phone);
    if (!existing) {
      existing = { id: userId, name: name || "User Telepon", email: null, phone, role: 'Pembeli', photoUrl: null };
      inMemoryUsers.push(existing);
    }
    userObj = existing;
  }

  res.json({
    success: true,
    token: token,
    id: userObj.id,
    name: userObj.name,
    email: userObj.email,
    phone: userObj.phone,
    role: userObj.role,
    photoUrl: userObj.photoUrl,
    address: userObj.address
  });
});

// Update Profile
app.post('/api/v1/auth/update-profile', async (req, res) => {
  const { userId, name, email, phone, address, photoUrl, role } = req.body;
  if (!userId) {
    return res.status(400).json({ success: false, message: "User ID is required" });
  }

  let userObj;
  if (mongoose.connection.readyState === 1) {
    let existing = await User.findOne({ id: userId });
    if (existing) {
      existing.name = name;
      existing.role = role;
      if (email !== undefined) existing.email = email;
      if (phone !== undefined) existing.phone = phone;
      if (address !== undefined) existing.address = address;
      if (photoUrl !== undefined) existing.photoUrl = photoUrl;
      await existing.save();
      userObj = existing.toObject();
    }
  } else {
    let existing = inMemoryUsers.find(u => u.id === userId);
    if (existing) {
      existing.name = name;
      existing.role = role;
      if (email !== undefined) existing.email = email;
      if (phone !== undefined) existing.phone = phone;
      if (address !== undefined) existing.address = address;
      if (photoUrl !== undefined) existing.photoUrl = photoUrl;
      userObj = existing;
    }
  }

  if (!userObj) {
    return res.status(404).json({ success: false, message: "User not found" });
  }

  res.json({
    success: true,
    token: jwt.sign({ id: userObj.id }, JWT_SECRET, { expiresIn: '7d' }),
    id: userObj.id,
    name: userObj.name,
    email: userObj.email,
    phone: userObj.phone,
    role: userObj.role,
    photoUrl: userObj.photoUrl,
    address: userObj.address
  });
});

// Get Products
app.get('/api/v1/products', async (req, res) => {
  if (mongoose.connection.readyState === 1) {
    const list = await Product.find();
    res.json(list);
  } else {
    res.json(inMemoryProducts);
  }
});

// Get Farmers
app.get('/api/v1/farmers', async (req, res) => {
  if (mongoose.connection.readyState === 1) {
    const list = await Farmer.find();
    res.json(list);
  } else {
    res.json(inMemoryFarmers);
  }
});

// Checkout API (Midtrans snap integration)
app.post('/api/v1/payment/checkout', async (req, res) => {
  const { userId, amount, items } = req.body;
  if (!userId || !amount) {
    return res.status(400).json({ success: false, message: "Missing required fields" });
  }

  const orderId = 'TRX-' + Date.now() + '-' + Math.floor(Math.random() * 1000);
  
  // Format items array for Midtrans Snap API payload
  let formattedItems = items ? items.map(item => ({
    id: item.id.toString(),
    price: Number(item.price),
    quantity: Number(item.quantity),
    name: item.name.length > 50 ? item.name.substring(0, 47) + "..." : item.name
  })) : [];

  // Midtrans requires the sum of (price * quantity) of all item_details to exactly match gross_amount.
  // We append an adjustment item if there's any mismatch due to shipping/packaging costs or discounts.
  const sumItems = formattedItems.reduce((acc, item) => acc + (item.price * item.quantity), 0);
  const diff = Number(amount) - sumItems;
  if (diff !== 0) {
    formattedItems.push({
      id: 'adjustment',
      price: diff,
      quantity: 1,
      name: 'Biaya Pengiriman & Diskon'
    });
  }

  const parameter = {
    transaction_details: {
      order_id: orderId,
      gross_amount: Number(amount)
    },
    credit_card: {
      secure: true
    },
    item_details: formattedItems,
    customer_details: {
      first_name: "Pembeli AgriMitra",
      email: "buyer@agrimitra.com"
    }
  };

  let snapToken = null;
  let redirectUrl = null;
  let isMock = false;

  try {
    console.log("Creating real Midtrans transaction with parameters:", JSON.stringify(parameter, null, 2));
    const transaction = await snap.createTransaction(parameter);
    snapToken = transaction.token;
    redirectUrl = transaction.redirect_url;
    console.log("Real Midtrans transaction created successfully. Redirect URL:", redirectUrl);
  } catch (err) {
    console.error("CRITICAL: Real Midtrans snap creation failed! Detail error:", err);
    isMock = true;
    snapToken = 'mock-token-' + orderId;
    redirectUrl = `${req.protocol}://${req.get('host')}/api/v1/payment/mock-page?orderId=${orderId}&amount=${amount}`;
  }

  const newOrder = {
    orderId,
    userId,
    amount,
    status: 'pending',
    snapToken,
    redirectUrl,
    items: items || [],
    createdAt: new Date()
  };

  if (mongoose.connection.readyState === 1) {
    await new Order(newOrder).save();
  } else {
    inMemoryOrders.push(newOrder);
  }

  res.json({
    success: true,
    message: "Permintaan checkout dibuat, silakan lakukan pembayaran",
    data: {
      orderId: orderId,
      topupId: orderId,
      amount: amount,
      status: "pending",
      expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
      payment: {
        token: snapToken,
        redirect_url: redirectUrl
      }
    }
  });
});

// HTML Mock Payment Simulator Page (Midtrans Snap Clone)
app.get('/api/v1/payment/mock-page', (req, res) => {
  const { orderId, amount } = req.query;
  
  // Format price helper
  const formattedPrice = Number(amount).toLocaleString('id-ID');

  res.send(`
    <html>
      <head>
        <title>Midtrans Secure Payment (Simulasi)</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>
          body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; background: #f0f2f5; color: #333333; margin: 0; padding: 12px; display: flex; align-items: center; justify-content: center; min-height: 100vh; }
          .container { background: white; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); max-width: 420px; width: 100%; overflow: hidden; display: flex; flex-direction: column; border: 1px solid #e5e7eb; }
          
          /* Header */
          .header { background: #011E41; color: white; padding: 16px 20px; display: flex; align-items: center; justify-content: space-between; }
          .header-title { font-size: 16px; font-weight: bold; letter-spacing: 0.5px; }
          .header-subtitle { font-size: 11px; color: #a5b4fc; }
          .amount-box { text-align: right; }
          .amount-val { font-size: 18px; font-weight: 800; color: #10B981; }
          .order-id { font-size: 10px; color: #9ca3af; text-align: right; margin-top: 2px; }

          /* Banner */
          .banner { background: #eff6ff; border-bottom: 1px solid #dbeafe; padding: 12px 20px; font-size: 12px; color: #1e40af; display: flex; align-items: center; gap: 8px; }
          
          /* Screen Container */
          .screens-wrapper { position: relative; width: 100%; overflow: hidden; min-height: 380px; }
          .screen { width: 100%; transition: transform 0.3s ease; box-sizing: border-box; }
          
          /* Main Menu Screen */
          #menu-screen { padding: 8px 0; }
          .section-title { font-size: 11px; font-weight: 700; color: #6b7280; text-transform: uppercase; padding: 12px 20px 4px 20px; letter-spacing: 1px; }
          .menu-item { display: flex; align-items: center; justify-content: space-between; padding: 14px 20px; cursor: pointer; border-bottom: 1px solid #f3f4f6; transition: background 0.2s; }
          .menu-item:hover { background: #f9fafb; }
          .menu-left { display: flex; align-items: center; gap: 14px; }
          .menu-icon { font-size: 20px; display: flex; align-items: center; justify-content: center; width: 28px; height: 28px; }
          .menu-text { display: flex; flex-direction: column; }
          .menu-title { font-size: 13px; font-weight: 600; color: #1f2937; }
          .menu-desc { font-size: 10px; color: #6b7280; margin-top: 2px; }
          .menu-arrow { font-size: 14px; color: #9ca3af; }
          
          /* Badges for E-wallet, banks etc */
          .badge-row { display: flex; gap: 4px; margin-top: 4px; }
          .badge { font-size: 8px; font-weight: bold; padding: 2px 5px; border-radius: 4px; text-transform: uppercase; }
          .bg-gopay { background: #eff6ff; color: #2563eb; border: 1px solid #bfdbfe; }
          .bg-dana { background: #f0fdf4; color: #16a34a; border: 1px solid #bbf7d0; }
          .bg-ovo { background: #faf5ff; color: #7c3aed; border: 1px solid #e9d5ff; }
          
          /* Detail Simulation Screens */
          .detail-screen { display: none; padding: 20px; flex-direction: column; }
          .btn-back-menu { align-self: flex-start; background: none; border: none; color: #2563eb; font-size: 12px; font-weight: 600; cursor: pointer; display: flex; align-items: center; gap: 6px; padding: 0; margin-bottom: 16px; }
          .detail-title { font-size: 15px; font-weight: bold; color: #1f2937; margin-bottom: 4px; }
          .detail-desc { font-size: 12px; color: #6b7280; margin-bottom: 20px; }
          
          /* Mock Visual Elements */
          .qr-box { background: white; border: 2px solid #e5e7eb; border-radius: 12px; padding: 16px; width: 140px; height: 140px; align-self: center; display: flex; flex-direction: column; align-items: center; justify-content: center; margin-bottom: 20px; }
          .qr-mock { font-size: 70px; margin-bottom: 6px; line-height: 1; }
          .qr-text { font-size: 9px; font-weight: bold; color: #9ca3af; letter-spacing: 1px; }
          
          .va-box { background: #f9fafb; border: 1px dashed #d1d5db; border-radius: 10px; padding: 14px 18px; display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
          .va-num { font-family: monospace; font-size: 17px; font-weight: bold; color: #1f2937; letter-spacing: 1px; }
          .va-copy { font-size: 11px; color: #2563eb; cursor: pointer; font-weight: 600; }
          
          /* Form card inputs */
          .form-group { display: flex; flex-direction: column; gap: 4px; margin-bottom: 14px; text-align: left; }
          .form-group label { font-size: 10px; font-weight: bold; color: #6b7280; text-transform: uppercase; }
          .form-group input { padding: 10px 12px; border: 1px solid #d1d5db; border-radius: 8px; font-size: 13px; }
          
          /* Action Buttons */
          .actions { display: flex; flex-direction: column; gap: 10px; margin-top: auto; }
          .btn-action { display: block; width: 100%; padding: 12px; border-radius: 8px; border: none; font-size: 13px; font-weight: bold; cursor: pointer; text-align: center; box-sizing: border-box; }
          .btn-primary { background: #10B981; color: white; box-shadow: 0 2px 8px rgba(16,185,129,0.25); }
          .btn-primary:hover { background: #059669; }
          .btn-secondary { background: #f3f4f6; color: #4b5563; border: 1px solid #e5e7eb; }
          .btn-secondary:hover { background: #e5e7eb; }
          .btn-danger { background: #EF4444; color: white; }
          .btn-danger:hover { background: #DC2626; }
        </style>
      </head>
      <body>
        <div class="container">
          
          <!-- Header (Midtrans Layout) -->
          <div class="header">
            <div>
              <div class="header-title">AgriMitra Secure Pay</div>
              <div class="header-subtitle">Mode Uji Pembayaran</div>
            </div>
            <div class="amount-box">
              <div class="amount-val">Rp ${formattedPrice}</div>
              <div class="order-id">${orderId}</div>
            </div>
          </div>
          
          <!-- Banner -->
          <div class="banner">
            <span>🛡️</span>
            <span>Simulator Pembayaran Midtrans Terintegrasi</span>
          </div>
          
          <!-- Screens Wrapper -->
          <div class="screens-wrapper">
            
            <!-- 1. MAIN MENU SCREEN -->
            <div id="menu-screen" class="screen">
              <div class="section-title">Metode Pembayaran</div>
              
              <!-- E-Wallet -->
              <div class="menu-item" onclick="openScreen('ewallet')">
                <div class="menu-left">
                  <div class="menu-icon">📱</div>
                  <div class="menu-text">
                    <span class="menu-title">E-Wallet (GoPay, DANA, OVO)</span>
                    <div class="badge-row">
                      <span class="badge bg-gopay">GoPay</span>
                      <span class="badge bg-dana">DANA</span>
                      <span class="badge bg-ovo">OVO</span>
                    </div>
                  </div>
                </div>
                <div class="menu-arrow">❯</div>
              </div>
              
              <!-- Bank Transfer -->
              <div class="menu-item" onclick="openScreen('bank')">
                <div class="menu-left">
                  <div class="menu-icon">🏦</div>
                  <div class="menu-text">
                    <span class="menu-title">Transfer Bank / Virtual Account</span>
                    <span class="menu-desc">BCA, Mandiri, BNI, BRI, BSI</span>
                  </div>
                </div>
                <div class="menu-arrow">❯</div>
              </div>
              
              <!-- Card Payment -->
              <div class="menu-item" onclick="openScreen('card')">
                <div class="menu-left">
                  <div class="menu-icon">💳</div>
                  <div class="menu-text">
                    <span class="menu-title">Kartu Kredit / Debit</span>
                    <span class="menu-desc">Visa, Mastercard, JCB, Amex</span>
                  </div>
                </div>
                <div class="menu-arrow">❯</div>
              </div>
              
              <!-- Retail counter -->
              <div class="menu-item" onclick="openScreen('retail')">
                <div class="menu-left">
                  <div class="menu-icon">🏪</div>
                  <div class="menu-text">
                    <span class="menu-title">Gerai Retail / OTC</span>
                    <span class="menu-desc">Indomaret, Alfamart</span>
                  </div>
                </div>
                <div class="menu-arrow">❯</div>
              </div>
              
              <!-- Paylater -->
              <div class="menu-item" onclick="openScreen('paylater')">
                <div class="menu-left">
                  <div class="menu-icon">💸</div>
                  <div class="menu-text">
                    <span class="menu-title">Cicilan Tanpa Kartu (PayLater)</span>
                    <span class="menu-desc">Akulaku PayLater, Kredivo</span>
                  </div>
                </div>
                <div class="menu-arrow">❯</div>
              </div>

              <!-- Cancel / Deny Option at the bottom -->
              <div style="padding: 16px 20px;">
                <button class="btn-action btn-secondary" onclick="simulatePayment('deny')">Batalkan Transaksi / Bayar Gagal</button>
              </div>
            </div>
            
            <!-- 2. DETAIL SCREEN: EWALLET (QRIS) -->
            <div id="screen-ewallet" class="detail-screen">
              <button class="btn-back-menu" onclick="showMainMenu()">❮ Kembali ke Menu</button>
              <div class="detail-title">Bayar dengan GoPay / QRIS</div>
              <div class="detail-desc">Scan kode QRIS di bawah ini dengan aplikasi e-wallet Anda.</div>
              
              <!-- Mock QR Code -->
              <div class="qr-box">
                <div class="qr-mock">🔳</div>
                <div class="qr-text">QRIS CODE</div>
              </div>
              
              <div class="actions">
                <button class="btn-action btn-primary" onclick="simulatePayment('settlement')">Simulasikan Bayar Sukses (GoPay)</button>
                <button class="btn-action btn-secondary" onclick="showMainMenu()">Pilih Metode Lain</button>
              </div>
            </div>
            
            <!-- 3. DETAIL SCREEN: BANK TRANSFER (VA) -->
            <div id="screen-bank" class="detail-screen">
              <button class="btn-back-menu" onclick="showMainMenu()">❮ Kembali ke Menu</button>
              <div class="detail-title">Virtual Account (BCA VA)</div>
              <div class="detail-desc">Silakan transfer nominal ke nomor Virtual Account di bawah ini.</div>
              
              <div class="va-box">
                <div class="va-num">88012${Math.floor(1000000000 + Math.random() * 9000000000)}</div>
                <div class="va-copy" onclick="alert('Nomor VA berhasil disalin!')">SALIN</div>
              </div>
              
              <div class="actions">
                <button class="btn-action btn-primary" onclick="simulatePayment('settlement')">Simulasikan Transfer Berhasil</button>
                <button class="btn-action btn-secondary" onclick="showMainMenu()">Pilih Bank Lain</button>
              </div>
            </div>
            
            <!-- 4. DETAIL SCREEN: CARD PAYMENT -->
            <div id="screen-card" class="detail-screen">
              <button class="btn-back-menu" onclick="showMainMenu()">❮ Kembali ke Menu</button>
              <div class="detail-title">Kartu Kredit / Debit</div>
              <div class="detail-desc">Masukkan rincian kartu kredit simulasi Anda.</div>
              
              <div class="form-group">
                <label>Nomor Kartu</label>
                <input type="text" placeholder="4111 1111 1111 1111" value="4111 1111 1111 1111">
              </div>
              <div style="display: flex; gap: 10px;">
                <div class="form-group" style="flex: 1;">
                  <label>Masa Berlaku</label>
                  <input type="text" placeholder="MM/YY" value="12/28">
                </div>
                <div class="form-group" style="flex: 1;">
                  <label>CVV</label>
                  <input type="password" placeholder="123" value="123">
                </div>
              </div>
              
              <div class="actions" style="margin-top: 10px;">
                <button class="btn-action btn-primary" onclick="simulatePayment('settlement')">Bayar Sekarang (Kartu)</button>
                <button class="btn-action btn-secondary" onclick="showMainMenu()">Batal</button>
              </div>
            </div>
            
            <!-- 5. DETAIL SCREEN: RETAIL (OTC) -->
            <div id="screen-retail" class="detail-screen">
              <button class="btn-back-menu" onclick="showMainMenu()">❮ Kembali ke Menu</button>
              <div class="detail-title">Bayar via Indomaret / Alfamart</div>
              <div class="detail-desc">Tunjukkan Kode Pembayaran berikut ke kasir gerai terdekat.</div>
              
              <div class="va-box">
                <div class="va-num">AGM${Math.floor(100000000 + Math.random() * 900000000)}</div>
                <div class="va-copy" onclick="alert('Kode pembayaran disalin!')">SALIN</div>
              </div>
              
              <div class="actions">
                <button class="btn-action btn-primary" onclick="simulatePayment('settlement')">Simulasikan Bayar Kasir Sukses</button>
                <button class="btn-action btn-secondary" onclick="showMainMenu()">Pilih Gerai Lain</button>
              </div>
            </div>
            
            <!-- 6. DETAIL SCREEN: PAYLATER -->
            <div id="screen-paylater" class="detail-screen">
              <button class="btn-back-menu" onclick="showMainMenu()">❮ Kembali ke Menu</button>
              <div class="detail-title">Akulaku / Kredivo PayLater</div>
              <div class="detail-desc">Beli sekarang dan bayar dengan tagihan bulan depan menggunakan akun Anda.</div>
              
              <div class="form-group">
                <label>Nomor Handphone Terdaftar</label>
                <input type="text" placeholder="08XXXXXXXXXX" value="081234567890">
              </div>
              
              <div class="actions">
                <button class="btn-action btn-primary" onclick="simulatePayment('settlement')">Konfirmasi & Bayar (PayLater)</button>
                <button class="btn-action btn-secondary" onclick="showMainMenu()">Batal</button>
              </div>
            </div>

          </div>
          
        </div>

        <script>
          // Simple screen navigator
          function openScreen(screenType) {
            document.getElementById('menu-screen').style.display = 'none';
            // Hide all details
            const details = document.getElementsByClassName('detail-screen');
            for(let i=0; i<details.length; i++) {
              details[i].style.display = 'none';
            }
            // Show target
            document.getElementById('screen-' + screenType).style.display = 'flex';
          }
          
          function showMainMenu() {
            // Hide all details
            const details = document.getElementsByClassName('detail-screen');
            for(let i=0; i<details.length; i++) {
              details[i].style.display = 'none';
            }
            document.getElementById('menu-screen').style.display = 'block';
          }
          
          // Payment webhook dispatcher
          function simulatePayment(status) {
            fetch('/api/v1/payment/mock-callback', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ orderId: '${orderId}', status: status })
            })
            .then(res => res.json())
            .then(data => {
              alert("Simulasi pembayaran " + (status === 'settlement' ? 'SUKSES' : 'BATAL/GAGAL') + " terkirim!");
              window.location.href = 'agrimitra://payment_result?status=' + (status === 'settlement' ? 'success' : 'failed') + '&orderId=${orderId}';
            })
            .catch(err => {
              alert("Gagal mengirim simulasi: " + err.message);
            });
          }
        </script>
      </body>
    </html>
  `);
});

// Mock Callback Handler
app.post('/api/v1/payment/mock-callback', async (req, res) => {
  const { orderId, status } = req.body;
  let order;
  if (mongoose.connection.readyState === 1) {
    order = await Order.findOne({ orderId });
    if (order) {
      order.status = status === 'settlement' ? 'success' : 'failed';
      await order.save();
    }
  } else {
    order = inMemoryOrders.find(o => o.orderId === orderId);
    if (order) {
      order.status = status === 'settlement' ? 'success' : 'failed';
    }
  }
  res.json({ success: true, message: `Status updated to ${order ? order.status : 'unknown'}` });
});

// Midtrans Real Callback Webhook Notification Endpoint
app.post('/api/v1/payment/notification', async (req, res) => {
  try {
    const body = req.body;
    const {
      order_id,
      status_code,
      gross_amount,
      signature_key,
      transaction_status,
      fraud_status,
    } = body;

    console.log(`[Midtrans Webhook] Menerima notifikasi untuk order_id: ${order_id}, status: ${transaction_status}`);

    // 1. Verifikasi Signature Key Midtrans untuk keamanan
    const serverKey = process.env.MIDTRANS_SERVER_KEY || 'SB-Mid-server-yU5bZ86wD-Mmx1Rj0S9423X7';
    // Rumus: SHA512(order_id + status_code + gross_amount + ServerKey)
    const computedHash = crypto
      .createHash("sha512")
      .update(order_id + status_code + gross_amount + serverKey)
      .digest("hex");

    if (computedHash !== signature_key) {
      console.error(`[Midtrans Webhook] Verifikasi signature GAGAL untuk order_id: ${order_id}`);
      return res.status(403).json({
        success: false,
        message: "Invalid signature key",
      });
    }

    console.log(`[Midtrans Webhook] Signature berhasil diverifikasi untuk order_id: ${order_id}`);

    // 2. Tentukan status pembayaran sukses/gagal
    const isSuccess =
      transaction_status === 'settlement' ||
      (transaction_status === 'capture' && fraud_status === 'accept');

    const isFailure =
      transaction_status === 'deny' ||
      transaction_status === 'cancel' ||
      transaction_status === 'expire';

    let newStatus = 'pending';
    if (isSuccess) {
      newStatus = 'success';
    } else if (isFailure) {
      newStatus = 'failed';
    }

    // 3. Update status order di DB (MongoDB / inMemoryOrders)
    let order;
    if (mongoose.connection.readyState === 1) {
      order = await Order.findOneAndUpdate(
        { orderId: order_id },
        { status: newStatus },
        { new: true }
      );
    } else {
      order = inMemoryOrders.find(o => o.orderId === order_id);
      if (order) {
        order.status = newStatus;
      }
    }

    console.log(`[Midtrans Webhook] Status order ${order_id} berhasil diperbarui di DB menjadi: ${newStatus}`);

    // Kirim response 200 OK ke Midtrans
    return res.status(200).json({
      success: true,
      message: "Webhook processed successfully"
    });
  } catch (error) {
    console.error("Error Midtrans Webhook:", error);
    return res.status(500).json({
      success: false,
      message: "Terjadi kesalahan saat memproses webhook",
      error: error.message,
    });
  }
});

// Start Server
app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});
