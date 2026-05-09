# Vertical Slice Architecture Refactor Instructions for BookBud Project

## EXECUTION PROTOCOL
- **BRANCH**: Work only on `refactor/vertical-slice-architecture` branch
- **BLOCKING**: DO NOT proceed to Phase N+1 until Phase N is fully complete
- **VERIFICATION**: After each phase, run build checks to ensure zero syntax errors
- **SCOPE**: No logic changes - only file movement and import updates

## CURRENT PROJECT STRUCTURE ANALYSIS

### Backend Structure (Java Spring Boot)
```
backend/bookbud/src/main/java/edu/cit/colo/bookbud/
├── BookbudApplication.java
├── config/
├── controller/          # 9 controllers
│   ├── AdminController.java
│   ├── AuthController.java
│   ├── BookController.java
│   ├── EarningsController.java
│   ├── NotificationController.java
│   ├── PaymentController.java
│   ├── TransactionController.java
│   ├── UserController.java
│   └── WishlistController.java
├── dto/                 # 29 DTOs
├── entity/              # 8 entities
│   ├── Book.java
│   ├── Notification.java
│   ├── PasswordResetToken.java
│   ├── Payment.java
│   ├── RefreshToken.java
│   ├── Transaction.java
│   ├── User.java
│   └── Wishlist.java
├── exception/           # 4 exceptions
├── repository/          # 8 repositories
│   ├── BookRepository.java
│   ├── NotificationRepository.java
│   ├── PasswordResetTokenRepository.java
│   ├── PaymentRepository.java
│   ├── RefreshTokenRepository.java
│   ├── TransactionRepository.java
│   ├── UserRepository.java
│   └── WishlistRepository.java
├── security/            # 2 security classes
└── service/             # 8 services
    ├── AdminService.java
    ├── AuthService.java
    ├── BookService.java
    ├── NotificationService.java
    ├── PaymentService.java
    ├── TransactionService.java
    ├── UserService.java
    └── WishlistService.java
```

### Frontend Structure (React)
```
web/bookbud/src/
├── App.js
├── components/          # 16 components
│   ├── AlertModal/
│   ├── AuthCard/
│   ├── ConfirmModal/
│   ├── Footer/
│   ├── FormInput/
│   ├── GoogleAuthButton/
│   ├── Navbar/
│   ├── Payment/
│   └── ProtectedRoute/
├── context/             # 1 context
├── hooks/               # 1 hook
├── pages/               # 33 pages
│   ├── Dashboard/
│   ├── LandingPage/
│   ├── admin/
│   ├── auth/
│   └── user/
├── services/            # 9 services
└── utils/               # 2 utilities
```

## REFACTORING PHASES

### PHASE 1: PRODUCTS (BOOKS) FEATURE
**Target Feature**: Book management system
**Backend Files**:
- controller/BookController.java
- service/BookService.java
- repository/BookRepository.java
- entity/Book.java
- dto/*Book*.java (all book-related DTOs)

**Frontend Files**:
- pages/user/BookBrowse.js
- pages/user/BookDetails.js
- pages/admin/BookManagement.js
- services/bookService.js
- components/*Book*.js (any book-related components)

**Actions**:
1. Create backend/src/main/java/edu/cit/colo/bookbud/features/books/
2. Create web/src/features/books/
3. Move all identified files to respective feature directories
4. Update package declarations: `package edu.cit.colo.bookbud.features.books;`
5. Update import statements in all moved files
6. Update React import paths
7. Run `mvn clean compile` and `npm run build` to verify

### PHASE 2: USERS FEATURE
**Target Feature**: User management and authentication
**Backend Files**:
- controller/UserController.java
- controller/AuthController.java
- service/UserService.java
- service/AuthService.java
- repository/UserRepository.java
- entity/User.java
- entity/PasswordResetToken.java
- entity/RefreshToken.java
- repository/PasswordResetTokenRepository.java
- repository/RefreshTokenRepository.java
- dto/*User*.java (all user-related DTOs)
- security/* (all security classes)

**Frontend Files**:
- pages/auth/* (all auth pages)
- pages/user/Profile.js
- pages/user/Settings.js
- services/authService.js
- services/userService.js
- components/AuthCard/
- components/GoogleAuthButton/
- components/ProtectedRoute/
- context/* (user context)

**Actions**:
1. Create backend/src/main/java/edu/cit/colo/bookbud/features/users/
2. Create web/src/features/users/
3. Move all identified files to respective feature directories
4. Update package declarations and imports
5. Update React import paths
6. Run build verification

### PHASE 3: CATEGORIES FEATURE
**Target Feature**: Book categorization and wishlist
**Backend Files**:
- controller/WishlistController.java
- service/WishlistService.java
- repository/WishlistRepository.java
- entity/Wishlist.java
- dto/*Wishlist*.java (all wishlist-related DTOs)

**Frontend Files**:
- pages/user/Wishlist.js
- pages/user/Categories.js
- services/wishlistService.js
- components/*Category*.js (any category-related components)

**Actions**:
1. Create backend/src/main/java/edu/cit/colo/bookbud/features/categories/
2. Create web/src/features/categories/
3. Move all identified files
4. Update package declarations and imports
5. Update React import paths
6. Run build verification

### PHASE 4: STOCK FEATURE
**Target Feature**: Transactions and payments
**Backend Files**:
- controller/TransactionController.java
- controller/PaymentController.java
- service/TransactionService.java
- service/PaymentService.java
- repository/TransactionRepository.java
- repository/PaymentRepository.java
- entity/Transaction.java
- entity/Payment.java
- dto/*Transaction*.java (all transaction-related DTOs)
- dto/*Payment*.java (all payment-related DTOs)

**Frontend Files**:
- pages/user/Checkout.js
- pages/user/TransactionHistory.js
- pages/user/PaymentMethods.js
- services/transactionService.js
- services/paymentService.js
- components/Payment/

**Actions**:
1. Create backend/src/main/java/edu/cit/colo/bookbud/features/stock/
2. Create web/src/features/stock/
3. Move all identified files
4. Update package declarations and imports
5. Update React import paths
6. Run build verification

### PHASE 5: DASHBOARD FEATURE
**Target Feature**: Admin dashboard and notifications
**Backend Files**:
- controller/AdminController.java
- controller/EarningsController.java
- controller/NotificationController.java
- service/AdminService.java
- service/NotificationService.java
- repository/NotificationRepository.java
- entity/Notification.java
- dto/*Admin*.java (all admin-related DTOs)
- dto/*Notification*.java (all notification-related DTOs)
- exception/* (all exceptions)

**Frontend Files**:
- pages/Dashboard/*
- pages/admin/* (all admin pages except BookManagement)
- services/adminService.js
- services/notificationService.js
- components/AlertModal/
- components/ConfirmModal/
- components/Footer/
- components/Navbar/

**Actions**:
1. Create backend/src/main/java/edu/cit/colo/bookbud/features/dashboard/
2. Create web/src/features/dashboard/
3. Move all identified files
4. Update package declarations and imports
5. Update React import paths
6. Run final build verification

## DETAILED REFACTORING WORKFLOW

### For Each Phase:

#### Step A: File Identification
```bash
# Backend - Find all related files
find backend/bookbud/src -name "*[feature-keyword]*" -type f
grep -r "[feature-keyword]" backend/bookbud/src --include="*.java"

# Frontend - Find all related files
find web/bookbud/src -name "*[feature-keyword]*" -type f
grep -r "[feature-keyword]" web/bookbud/src --include="*.js" --include="*.jsx"
```

#### Step B: Directory Structure Creation
```bash
# Backend
mkdir -p backend/bookbud/src/main/java/edu/cit/colo/bookbud/features/[feature-name]/{controller,service,repository,entity,dto}

# Frontend
mkdir -p web/bookbud/src/features/[feature-name]/{components,pages,services,utils}
```

#### Step C: File Movement
```bash
# Move backend files
mv backend/bookbud/src/main/java/edu/cit/colo/bookbud/controller/[Feature]Controller.java backend/bookbud/src/main/java/edu/cit/colo/bookbud/features/[feature-name]/controller/
mv backend/bookbud/src/main/java/edu/cit/colo/bookbud/service/[Feature]Service.java backend/bookbud/src/main/java/edu/cit/colo/bookbud/features/[feature-name]/service/
mv backend/bookbud/src/main/java/edu/cit/colo/bookbud/repository/[Feature]Repository.java backend/bookbud/src/main/java/edu/cit/colo/bookbud/features/[feature-name]/repository/
mv backend/bookbud/src/main/java/edu/cit/colo/bookbud/entity/[Feature].java backend/bookbud/src/main/java/edu/cit/colo/bookbud/features/[feature-name]/entity/

# Move frontend files
mv web/bookbud/src/pages/[feature]/* web/bookbud/src/features/[feature-name]/pages/
mv web/bookbud/src/components/[feature]/* web/bookbud/src/features/[feature-name]/components/
mv web/bookbud/src/services/[feature]Service.js web/bookbud/src/features/[feature-name]/services/
```

#### Step D: Package Declaration Updates
For each moved Java file, update:
```java
// Old: package edu.cit.colo.bookbud.controller;
// New: package edu.cit.colo.bookbud.features.[feature-name].controller;
```

#### Step E: Import Statement Updates
Update all import statements across the entire codebase:
```bash
# Find files that import moved classes
grep -r "import edu.cit.colo.bookbud.controller" backend/bookbud/src --include="*.java"
grep -r "import edu.cit.colo.bookbud.service" backend/bookbud/src --include="*.java"
grep -r "import edu.cit.colo.bookbud.repository" backend/bookbud/src --include="*.java"
grep -r "import edu.cit.colo.bookbud.entity" backend/bookbud/src --include="*.java"

# Update imports to new package structure
# Old: import edu.cit.colo.bookbud.controller.BookController;
# New: import edu.cit.colo.bookbud.features.books.controller.BookController;
```

#### Step F: React Import Path Updates
Update all React import statements:
```bash
# Find files that import moved components/services
grep -r "from.*pages/[feature]" web/bookbud/src --include="*.js" --include="*.jsx"
grep -r "from.*components/[feature]" web/bookbud/src --include="*.js" --include="*.jsx"
grep -r "from.*services/[feature]" web/bookbud/src --include="*.js" --include="*.jsx"

# Update import paths
# Old: import BookService from '../services/bookService';
# New: import BookService from '../features/books/services/bookService';
```

#### Step G: Build Verification
```bash
# Backend
cd backend/bookbud
mvn clean compile
mvn test

# Frontend
cd web/bookbud
npm run build
npm test
```

## FINAL PROJECT STRUCTURE (After Refactor)

### Backend Structure
```
backend/bookbud/src/main/java/edu/cit/colo/bookbud/
├── BookbudApplication.java
├── config/              # Shared configuration
├── features/
│   ├── books/
│   │   ├── controller/
│   │   │   └── BookController.java
│   │   ├── service/
│   │   │   └── BookService.java
│   │   ├── repository/
│   │   │   └── BookRepository.java
│   │   ├── entity/
│   │   │   └── Book.java
│   │   └── dto/
│   │       └── *Book*.java
│   ├── users/
│   │   ├── controller/
│   │   │   ├── UserController.java
│   │   │   └── AuthController.java
│   │   ├── service/
│   │   │   ├── UserService.java
│   │   │   └── AuthService.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   ├── PasswordResetTokenRepository.java
│   │   │   └── RefreshTokenRepository.java
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── PasswordResetToken.java
│   │   │   └── RefreshToken.java
│   │   ├── security/
│   │   │   └── *Security*.java
│   │   └── dto/
│   │       └── *User*.java
│   ├── categories/
│   │   ├── controller/
│   │   │   └── WishlistController.java
│   │   ├── service/
│   │   │   └── WishlistService.java
│   │   ├── repository/
│   │   │   └── WishlistRepository.java
│   │   ├── entity/
│   │   │   └── Wishlist.java
│   │   └── dto/
│   │       └── *Wishlist*.java
│   ├── stock/
│   │   ├── controller/
│   │   │   ├── TransactionController.java
│   │   │   └── PaymentController.java
│   │   ├── service/
│   │   │   ├── TransactionService.java
│   │   │   └── PaymentService.java
│   │   ├── repository/
│   │   │   ├── TransactionRepository.java
│   │   │   └── PaymentRepository.java
│   │   ├── entity/
│   │   │   ├── Transaction.java
│   │   │   └── Payment.java
│   │   └── dto/
│   │       ├── *Transaction*.java
│   │       └── *Payment*.java
│   └── dashboard/
│       ├── controller/
│       │   ├── AdminController.java
│       │   ├── EarningsController.java
│       │   └── NotificationController.java
│       ├── service/
│       │   ├── AdminService.java
│       │   └── NotificationService.java
│       ├── repository/
│       │   └── NotificationRepository.java
│       ├── entity/
│       │   └── Notification.java
│       ├── exception/
│       │   └── *Exception*.java
│       └── dto/
│           ├── *Admin*.java
│           └── *Notification*.java
```

### Frontend Structure
```
web/bookbud/src/
├── App.js
├── features/
│   ├── books/
│   │   ├── components/
│   │   │   └── *Book*.js
│   │   ├── pages/
│   │   │   ├── BookBrowse.js
│   │   │   ├── BookDetails.js
│   │   │   └── BookManagement.js
│   │   └── services/
│   │       └── bookService.js
│   ├── users/
│   │   ├── components/
│   │   │   ├── AuthCard/
│   │   │   ├── GoogleAuthButton/
│   │   │   └── ProtectedRoute/
│   │   ├── pages/
│   │   │   ├── auth/
│   │   │   ├── Profile.js
│   │   │   └── Settings.js
│   │   ├── services/
│   │   │   ├── authService.js
│   │   │   └── userService.js
│   │   └── context/
│   │       └── *Context*.js
│   ├── categories/
│   │   ├── components/
│   │   │   └── *Category*.js
│   │   ├── pages/
│   │   │   ├── Wishlist.js
│   │   │   └── Categories.js
│   │   └── services/
│   │       └── wishlistService.js
│   ├── stock/
│   │   ├── components/
│   │   │   └── Payment/
│   │   ├── pages/
│   │   │   ├── Checkout.js
│   │   │   ├── TransactionHistory.js
│   │   │   └── PaymentMethods.js
│   │   └── services/
│   │       ├── transactionService.js
│   │       └── paymentService.js
│   └── dashboard/
│       ├── components/
│       │   ├── AlertModal/
│       │   ├── ConfirmModal/
│       │   ├── Footer/
│       │   └── Navbar/
│       ├── pages/
│       │   ├── Dashboard/
│       │   └── admin/
│       └── services/
│           ├── adminService.js
│           └── notificationService.js
├── hooks/               # Shared hooks
└── utils/               # Shared utilities
```

## CRITICAL SUCCESS CRITERIA

### After Each Phase:
1. **Zero Compilation Errors**: `mvn clean compile` succeeds
2. **Zero Build Errors**: `npm run build` succeeds
3. **All Tests Pass**: Both backend and frontend tests pass
4. **No Broken Imports**: All import statements resolve correctly
5. **Functionality Preserved**: All existing features work exactly as before

### Final Verification:
1. **Complete Build**: Full application builds and runs successfully
2. **Feature Testing**: All 5 features (books, users, categories, stock, dashboard) function correctly
3. **No Regression**: Existing functionality is preserved
4. **Clean Structure**: Code is organized by feature, not layer
5. **Documentation**: README.md updated with new structure

## EXECUTION COMMANDS SUMMARY

### Initial Setup
```bash
# Create and checkout branch
git checkout -b refactor/vertical-slice-architecture

# Create backup of current structure
cp -r backend/bookbud/src backend/bookbud/src_backup
cp -r web/bookbud/src web/bookbud/src_backup
```

### Phase Execution Template
```bash
# For each phase [feature-name]:
echo "Starting Phase: [feature-name]"

# Step A: Identify files
# (Manual identification based on file names and content)

# Step B: Create directories
mkdir -p backend/bookbud/src/main/java/edu/cit/colo/bookbud/features/[feature-name]/{controller,service,repository,entity,dto}
mkdir -p web/bookbud/src/features/[feature-name]/{components,pages,services,utils}

# Step C: Move files
# (Move identified files to new locations)

# Step D: Update package declarations
# (Update Java package declarations)

# Step E: Update import statements
# (Update all import statements across codebase)

# Step F: Update React imports
# (Update React import paths)

# Step G: Build verification
cd backend/bookbud && mvn clean compile
cd web/bookbud && npm run build

echo "Phase [feature-name] completed successfully"
```

### Final Commands
```bash
# Final verification
cd backend/bookbud && mvn clean test
cd web/bookbud && npm test

# Commit changes
git add .
git commit -m "feat: implement vertical slice architecture refactor"

# Push to remote
git push origin refactor/vertical-slice-architecture
```

## TROUBLESHOOTING GUIDE

### Common Issues:
1. **Import Resolution**: Use IDE's "Optimize imports" feature
2. **Package Structure**: Verify package declarations match directory structure
3. **Circular Dependencies**: Check for dependencies between features
4. **Missing Files**: Ensure all related files are moved together
5. **Build Failures**: Check console output for specific error locations

### Recovery:
```bash
# If critical failure occurs, restore from backup
rm -rf backend/bookbud/src
rm -rf web/bookbud/src
mv backend/bookbud/src_backup backend/bookbud/src
mv web/bookbud/src_backup web/bookbud/src
```

## REFACTORING SUMMARY TEMPLATE

### SECTION 2 — REFACTORING SUMMARY
**Branch & Timeline**
- Branch Created From: main
- Date Refactoring Started: [Date]
- Date Refactoring Completed: [Date]

**Description of Changes Made**
[Write a paragraph summarizing what you refactored. Describe the architectural shift from layer-based to feature-based Vertical Slice Architecture and which files/folders were moved.]

**Scope of Refactoring**
| Scope | Included? | Brief Description |
|-------|-----------|------------------|
| Backend | [Yes/No] | [Description] |
| Web Frontend | [Yes/No] | [Description] |
| Mobile Application | [Yes/No] | [Description] |

**Refactoring Goals Checklist**
- [ ] Organized project by feature/module
- [ ] All existing features remain functional
- [ ] Clean code and naming conventions maintained
- [ ] Branch pushed to GitHub with complete commit history

### SECTION 3 — UPDATED PROJECT STRUCTURE
**Before Refactoring (Old Structure)**
[Paste your old folder/file tree here]

**After Refactoring (New Vertical Slice Structure)**
[Paste your new folder/file tree here]
