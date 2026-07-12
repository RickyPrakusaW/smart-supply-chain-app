package com.agroSystem.app.features.admin

import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agroSystem.app.R
import com.agroSystem.app.data.local.AppDatabase
import com.agroSystem.app.data.models.Product
import com.agroSystem.app.data.models.User
import com.agroSystem.app.data.models.bindImageTo
import com.agroSystem.app.features.shared.MainSharedViewModel
import com.agroSystem.app.features.payment.TransactionHistoryAdapter
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.util.Locale

class AdminDashboardFragment : Fragment() {

    private val sharedViewModel: MainSharedViewModel by activityViewModels()

    private lateinit var tabLayout: TabLayout
    private lateinit var scrollReports: View
    private lateinit var layoutManageData: View
    private lateinit var textStatUsers: TextView
    private lateinit var textStatProducts: TextView
    private lateinit var textStatOrders: TextView
    private lateinit var textStatRevenue: TextView
    private lateinit var rvTransactions: RecyclerView
    private lateinit var textEmptyTransactions: View
    private lateinit var rvUsers: RecyclerView
    private lateinit var rvProducts: RecyclerView
    private lateinit var progressLoader: ProgressBar
    private lateinit var btnBack: View

    private lateinit var transactionAdapter: TransactionHistoryAdapter
    private lateinit var userAdapter: AdminUserAdapter
    private lateinit var productAdapter: AdminProductAdapter

    private val exportUsersLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            writeUsersCsvToUri(uri)
        }
    }

    private val exportTransactionsLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            writeTransactionsCsvToUri(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false)

        tabLayout = view.findViewById(R.id.tab_layout)
        scrollReports = view.findViewById(R.id.scroll_reports)
        layoutManageData = view.findViewById(R.id.layout_manage_data)
        textStatUsers = view.findViewById(R.id.text_stat_users)
        textStatProducts = view.findViewById(R.id.text_stat_products)
        textStatOrders = view.findViewById(R.id.text_stat_orders)
        textStatRevenue = view.findViewById(R.id.text_stat_revenue)
        rvTransactions = view.findViewById(R.id.rv_admin_transactions)
        textEmptyTransactions = view.findViewById(R.id.text_empty_transactions)
        rvUsers = view.findViewById(R.id.rv_admin_users)
        rvProducts = view.findViewById(R.id.rv_admin_products)
        progressLoader = view.findViewById(R.id.progress_loader)
        btnBack = view.findViewById(R.id.btn_back)

        setupTabs()
        setupRecyclerViews()
        setupListeners()

        // Bind Export Buttons
        view.findViewById<View>(R.id.btn_export_users).setOnClickListener {
            exportUsersLauncher.launch("daftar_pengguna_${System.currentTimeMillis()}.csv")
        }
        view.findViewById<View>(R.id.btn_export_transactions).setOnClickListener {
            exportTransactionsLauncher.launch("laporan_penjualan_${System.currentTimeMillis()}.csv")
        }

        loadAdminData()

        return view
    }

    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    scrollReports.visibility = View.VISIBLE
                    layoutManageData.visibility = View.GONE
                } else {
                    scrollReports.visibility = View.GONE
                    layoutManageData.visibility = View.VISIBLE
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerViews() {
        // Transactions
        transactionAdapter = TransactionHistoryAdapter(emptyList()) { }
        rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        rvTransactions.adapter = transactionAdapter

        // Users
        userAdapter = AdminUserAdapter(emptyList()) { user ->
            showDeleteUserConfirmation(user)
        }
        rvUsers.layoutManager = LinearLayoutManager(requireContext())
        rvUsers.adapter = userAdapter

        // Products
        productAdapter = AdminProductAdapter(emptyList()) { product ->
            showDeleteProductConfirmation(product)
        }
        rvProducts.layoutManager = LinearLayoutManager(requireContext())
        rvProducts.adapter = productAdapter
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadAdminData() {
        progressLoader.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()

        // 1. Fetch Users
        db.collection("users").get()
            .addOnSuccessListener { usersResult ->
                val usersList = usersResult.map { doc ->
                    User(
                        id = doc.getString("id") ?: doc.id,
                        name = doc.getString("name") ?: "",
                        email = doc.getString("email"),
                        phone = doc.getString("phone"),
                        role = doc.getString("role") ?: "Pembeli",
                        token = null,
                        photoUrl = doc.getString("photoUrl"),
                        address = doc.getString("address")
                    )
                }
                userAdapter.updateUsers(usersList)
                textStatUsers.text = usersList.size.toString()

                // 2. Fetch Products
                db.collection("products").get()
                    .addOnSuccessListener { productsResult ->
                        val productsList = productsResult.map { doc ->
                            Product(
                                id = doc.getLong("id")?.toInt() ?: 0,
                                name = doc.getString("name") ?: "",
                                farmer = doc.getString("farmer") ?: "",
                                rating = doc.getString("rating") ?: "5.0",
                                price = doc.getLong("price")?.toInt() ?: 0,
                                unit = doc.getString("unit") ?: "1 kg",
                                imageResId = doc.getLong("imageResId")?.toInt() ?: R.drawable.padi,
                                category = doc.getString("category") ?: "Lainnya",
                                isDiscounted = doc.getBoolean("isDiscounted") ?: false,
                                originalPrice = doc.getLong("originalPrice")?.toInt() ?: 0,
                                isEcoFriendly = doc.getBoolean("isEcoFriendly") ?: false,
                                deliveryDays = doc.getLong("deliveryDays")?.toInt() ?: 1,
                                protein = doc.getString("protein") ?: "3g",
                                fat = doc.getString("fat") ?: "5g",
                                carbs = doc.getString("carbs") ?: "4.7g",
                                calories = doc.getString("calories") ?: "64 Kcal",
                                ingredients = doc.getString("ingredients") ?: "",
                                shelfLife = doc.getString("shelfLife") ?: "5 Hari",
                                storage = doc.getString("storage") ?: "Suhu Dingin (+2°C s.d +6°C)",
                                packaging = doc.getString("packaging") ?: "Botol Kaca steril 1 Liter (Ramah Lingkungan)",
                                ownerId = doc.getString("ownerId"),
                                imageBytes = doc.getString("imageBytes")
                            )
                        }
                        productAdapter.updateProducts(productsList)
                        textStatProducts.text = productsList.size.toString()

                        // 3. Fetch Orders (Transactions)
                        db.collection("orders").get()
                            .addOnSuccessListener { ordersResult ->
                                val ordersList = ordersResult.map { doc ->
                                    val itemsRaw = doc.get("items") as? List<Map<String, Any>>
                                    val checkoutItems = itemsRaw?.map { itemMap ->
                                        com.agroSystem.app.data.remote.CheckoutItem(
                                            id = (itemMap["id"] as? Long)?.toInt() ?: 0,
                                            name = itemMap["name"] as? String ?: "",
                                            price = (itemMap["price"] as? Long)?.toInt() ?: 0,
                                            quantity = (itemMap["quantity"] as? Long)?.toInt() ?: 0,
                                            ownerId = itemMap["ownerId"] as? String
                                        )
                                    }
                                    
                                    com.agroSystem.app.data.remote.OrderItemResponse(
                                        orderId = doc.getString("orderId") ?: doc.id,
                                        userId = doc.getString("userId"),
                                        amount = doc.getLong("amount")?.toInt() ?: 0,
                                        status = doc.getString("status") ?: "pending",
                                        createdAt = doc.getString("createdAt") ?: doc.getTimestamp("createdAt")?.toDate()?.toString() ?: "",
                                        items = checkoutItems,
                                        payment = null
                                    )
                                }

                                transactionAdapter.updateOrders(ordersList)
                                textStatOrders.text = ordersList.size.toString()

                                if (ordersList.isEmpty()) {
                                    textEmptyTransactions.visibility = View.VISIBLE
                                    rvTransactions.visibility = View.GONE
                                } else {
                                    textEmptyTransactions.visibility = View.GONE
                                    rvTransactions.visibility = View.VISIBLE
                                }

                                // Sum success revenue
                                val successRevenue = ordersList
                                    .filter { it.status == "success" || it.status == "completed" || it.status == "settlement" }
                                    .sumOf { it.amount }
                                
                                textStatRevenue.text = "Rp " + java.text.NumberFormat.getNumberInstance(Locale("id", "ID")).format(successRevenue)
                                
                                progressLoader.visibility = View.GONE
                            }
                            .addOnFailureListener { e ->
                                progressLoader.visibility = View.GONE
                                Log.e("AdminDashboard", "Failed to fetch orders", e)
                            }
                    }
                    .addOnFailureListener { e ->
                        progressLoader.visibility = View.GONE
                        Log.e("AdminDashboard", "Failed to fetch products", e)
                    }
            }
            .addOnFailureListener { e ->
                progressLoader.visibility = View.GONE
                Toast.makeText(requireContext(), "Gagal memuat data admin: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteUserConfirmation(user: User) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Pengguna?")
            .setMessage("Apakah Anda yakin ingin menghapus akun '${user.name}'? Seluruh data profil pengguna ini akan dihapus dari Firestore.")
            .setPositiveButton("Hapus") { _, _ ->
                progressLoader.visibility = View.VISIBLE
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(user.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "User berhasil dihapus", Toast.LENGTH_SHORT).show()
                        loadAdminData()
                    }
                    .addOnFailureListener { e ->
                        progressLoader.visibility = View.GONE
                        Toast.makeText(requireContext(), "Gagal menghapus user: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDeleteProductConfirmation(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Produk?")
            .setMessage("Apakah Anda yakin ingin menghapus produk '${product.name}'? Produk akan dihapus secara permanen dari sistem.")
            .setPositiveButton("Hapus") { _, _ ->
                progressLoader.visibility = View.VISIBLE
                val db = FirebaseFirestore.getInstance()
                db.collection("products").document(product.id.toString()).delete()
                    .addOnSuccessListener {
                        // Also delete from local Room cache in a coroutine
                        lifecycleScope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    val localDb = AppDatabase.getDatabase(requireContext())
                                    localDb.productDao().deleteProductById(product.id)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            
                            // Remove from local shared viewmodel cache as well
                            sharedViewModel.allProducts.removeAll { it.id == product.id }
                            sharedViewModel.productsList.value = sharedViewModel.allProducts

                            Toast.makeText(requireContext(), "Produk berhasil dihapus", Toast.LENGTH_SHORT).show()
                            loadAdminData()
                        }
                    }
                    .addOnFailureListener { e ->
                        progressLoader.visibility = View.GONE
                        Toast.makeText(requireContext(), "Gagal menghapus produk: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun writeUsersCsvToUri(uri: android.net.Uri) {
        try {
            val outputStream = requireContext().contentResolver.openOutputStream(uri) ?: return
            val writer = java.io.BufferedWriter(java.io.OutputStreamWriter(outputStream, "UTF-8"))
            
            // Write BOM for Excel compatibility with UTF-8
            writer.write("\uFEFF")
            
            // CSV Header
            writer.write("ID,Nama,Email,Telepon,Peran (Role),Alamat\n")
            
            // Write User rows
            val usersList = userAdapter.getUsersList()
            usersList.forEach { user ->
                val id = user.id.replace("\"", "\"\"")
                val name = user.name.replace("\"", "\"\"")
                val email = (user.email ?: "").replace("\"", "\"\"")
                val phone = (user.phone ?: "").replace("\"", "\"\"")
                val role = user.role.replace("\"", "\"\"")
                val address = (user.address ?: "").replace("\"", "\"\"")
                
                writer.write("\"$id\",\"$name\",\"$email\",\"$phone\",\"$role\",\"$address\"\n")
            }
            
            writer.flush()
            writer.close()
            outputStream.close()
            Toast.makeText(requireContext(), "Daftar pengguna berhasil diekspor ke CSV/Excel!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Gagal mengekspor data: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun writeTransactionsCsvToUri(uri: android.net.Uri) {
        try {
            val outputStream = requireContext().contentResolver.openOutputStream(uri) ?: return
            val writer = java.io.BufferedWriter(java.io.OutputStreamWriter(outputStream, "UTF-8"))
            
            // Write BOM
            writer.write("\uFEFF")
            
            // CSV Header
            writer.write("ID Transaksi,ID Pembeli,Total Belanja,Status Pembayaran,Tanggal Pembuatan\n")
            
            val ordersList = transactionAdapter.getOrdersList()
            ordersList.forEach { order ->
                val orderId = order.orderId.replace("\"", "\"\"")
                val userId = (order.userId ?: "").replace("\"", "\"\"")
                val amount = order.amount
                val status = order.status.replace("\"", "\"\"")
                val createdAt = order.createdAt.replace("\"", "\"\"")
                
                writer.write("\"$orderId\",\"$userId\",$amount,\"$status\",\"$createdAt\"\n")
            }
            
            writer.flush()
            writer.close()
            outputStream.close()
            Toast.makeText(requireContext(), "Laporan transaksi berhasil diekspor ke CSV/Excel!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Gagal mengekspor data: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

class AdminUserAdapter(
    private var users: List<User>,
    private val onDeleteClick: (User) -> Unit
) : RecyclerView.Adapter<AdminUserAdapter.UserViewHolder>() {

    fun updateUsers(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }

    fun getUsersList(): List<User> = users

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user, onDeleteClick)
    }

    override fun getItemCount(): Int = users.size

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val txtName: TextView = view.findViewById(R.id.txt_user_name)
        private val txtContact: TextView = view.findViewById(R.id.txt_user_contact)
        private val txtRole: TextView = view.findViewById(R.id.txt_user_role)
        private val btnDelete: Button = view.findViewById(R.id.btn_delete_user)

        fun bind(user: User, onDeleteClick: (User) -> Unit) {
            txtName.text = user.name
            val email = user.email ?: "-"
            val phone = user.phone ?: "-"
            txtContact.text = "$email | $phone"
            txtRole.text = "Peran: ${user.role}"

            // Admin cannot delete themselves
            val currentFirebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            val currentCleanEmail = currentFirebaseUser?.email?.replace(Regex("[^a-zA-Z0-9]"), "") ?: ""
            val currentGoogleId = "google_$currentCleanEmail"
            val currentCleanPhone = currentFirebaseUser?.phoneNumber?.replace(Regex("[^0-9]"), "") ?: ""
            val currentPhoneId = "phone_$currentCleanPhone"

            if (user.id == currentGoogleId || user.id == currentPhoneId || user.id == currentFirebaseUser?.uid) {
                btnDelete.visibility = View.GONE
            } else {
                btnDelete.visibility = View.VISIBLE
                btnDelete.setOnClickListener { onDeleteClick(user) }
            }
        }
    }
}

class AdminProductAdapter(
    private var products: List<Product>,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<AdminProductAdapter.ProductViewHolder>() {

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_seller_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product, onDeleteClick)
    }

    override fun getItemCount(): Int = products.size

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageProduct: ImageView = itemView.findViewById(R.id.image_product)
        private val textTitle: TextView = itemView.findViewById(R.id.text_title)
        private val textCategory: TextView = itemView.findViewById(R.id.text_category)
        private val textPrice: TextView = itemView.findViewById(R.id.text_price)
        private val btnEdit: View = itemView.findViewById(R.id.btn_edit)
        private val btnDelete: View = itemView.findViewById(R.id.btn_delete)

        fun bind(product: Product, onDeleteClick: (Product) -> Unit) {
            product.bindImageTo(imageProduct)
            textTitle.text = product.name
            textCategory.text = product.category
            textPrice.text = "Rp ${formatPrice(product.price)} / ${product.unit}"

            // Hide edit button for admin moderator list
            btnEdit.visibility = View.GONE

            btnDelete.setOnClickListener { onDeleteClick(product) }
        }

        private fun formatPrice(price: Int): String {
            return java.text.NumberFormat.getNumberInstance(Locale("id", "ID")).format(price)
        }
    }
}
