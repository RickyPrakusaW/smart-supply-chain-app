const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const jwt = require('jsonwebtoken');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 5000;
const JWT_SECRET = process.env.JWT_SECRET || 'agrimitra_jwt_super_secret_key';

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
  photoUrl: { type: String, default: null }
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

// In-Memory Database Fallbacks
let inMemoryUsers = [];
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
    photoUrl: userObj.photoUrl
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
    photoUrl: userObj.photoUrl
  });
});

// Update Profile
app.post('/api/v1/auth/update-profile', async (req, res) => {
  const { userId, name, role } = req.body;
  if (!userId) {
    return res.status(400).json({ success: false, message: "User ID is required" });
  }

  let userObj;
  if (mongoose.connection.readyState === 1) {
    let existing = await User.findOne({ id: userId });
    if (existing) {
      existing.name = name;
      existing.role = role;
      await existing.save();
      userObj = existing.toObject();
    }
  } else {
    let existing = inMemoryUsers.find(u => u.id === userId);
    if (existing) {
      existing.name = name;
      existing.role = role;
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
    photoUrl: userObj.photoUrl
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

// Start Server
app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});
