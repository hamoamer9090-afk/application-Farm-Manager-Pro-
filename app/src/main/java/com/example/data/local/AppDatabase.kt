package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {
    @Query("SELECT * FROM farms")
    fun getAllFarms(): Flow<List<FarmEntity>>

    @Query("SELECT * FROM farms WHERE name = :name LIMIT 1")
    suspend fun getFarmByName(name: String): FarmEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarm(farm: FarmEntity)

    @Delete
    suspend fun deleteFarm(farm: FarmEntity)

    @Query("DELETE FROM farms WHERE name = :name")
    suspend fun deleteFarmByName(name: String)
    @Query("UPDATE farms SET name = :newName WHERE name = :oldName")
    suspend fun updateFarmName(oldName: String, newName: String)

    @Query("UPDATE farms SET password = :newPassword WHERE name = :farmName")
    suspend fun updateFarmPassword(farmName: String, newPassword: String)
}

@Dao
interface AnimalDao {
    @Query("UPDATE animals SET farmName = :newName WHERE farmName = :oldName")
    suspend fun updateFarmName(oldName: String, newName: String)
    @Query("SELECT * FROM animals WHERE farmName = :farmName AND isArchived = 0 ORDER BY id DESC")
    fun getAnimalsByFarm(farmName: String): Flow<List<AnimalEntity>>

    @Query("SELECT * FROM animals WHERE farmName = :farmName ORDER BY id DESC")
    fun getAllAnimalsForArchiveByFarm(farmName: String): Flow<List<AnimalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimal(animal: AnimalEntity): Long

    @Update
    suspend fun updateAnimal(animal: AnimalEntity)

    @Delete
    suspend fun deleteAnimal(animal: AnimalEntity)

    @Query("DELETE FROM animals WHERE farmName = :farmName")
    suspend fun deleteAllAnimalsOfFarm(farmName: String)
}

@Dao
interface FeedDao {
    @Query("UPDATE feeds SET farmName = :newName WHERE farmName = :oldName")
    suspend fun updateFarmName(oldName: String, newName: String)

    @Query("SELECT * FROM feeds WHERE farmName = :farmName ORDER BY id DESC")
    fun getFeedsByFarm(farmName: String): Flow<List<FeedEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeed(feed: FeedEntity): Long

    @Update
    suspend fun updateFeed(feed: FeedEntity)

    @Delete
    suspend fun deleteFeed(feed: FeedEntity)

    @Query("DELETE FROM feeds WHERE farmName = :farmName")
    suspend fun deleteAllFeedsOfFarm(farmName: String)
}

@Dao
interface PersonDao {
    @Query("UPDATE people SET farmName = :newName WHERE farmName = :oldName")
    suspend fun updateFarmName(oldName: String, newName: String)

    @Query("SELECT * FROM people WHERE farmName = :farmName AND isArchived = 0 ORDER BY id DESC")
    fun getPeopleByFarm(farmName: String): Flow<List<PersonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: PersonEntity): Long

    @Update
    suspend fun updatePerson(person: PersonEntity)

    @Delete
    suspend fun deletePerson(person: PersonEntity)

    @Query("SELECT * FROM people WHERE id = :id LIMIT 1")
    suspend fun getPersonById(id: Int): PersonEntity?

    @Query("DELETE FROM people WHERE farmName = :farmName")
    suspend fun deleteAllPeopleOfFarm(farmName: String)
}

@Dao
interface TransactionDao {
    @Query("UPDATE transactions SET farmName = :newName WHERE farmName = :oldName")
    suspend fun updateFarmName(oldName: String, newName: String)

    @Query("SELECT * FROM transactions WHERE farmName = :farmName AND isArchived = 0 ORDER BY id DESC")
    fun getTransactionsByFarm(farmName: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE farmName = :farmName ORDER BY id DESC")
    fun getAllTransactionsForArchiveByFarm(farmName: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE farmName = :farmName")
    suspend fun deleteAllTransactionsOfFarm(farmName: String)

    @Query("SELECT * FROM transactions WHERE associatedPersonId = :personId AND isArchived = 0")
    suspend fun getTransactionsByPersonDirect(personId: Int): List<TransactionEntity>
}

@Dao
interface NoteDao {
    @Query("UPDATE notes SET farmName = :newName WHERE farmName = :oldName")
    suspend fun updateFarmName(oldName: String, newName: String)

    @Query("SELECT * FROM notes WHERE farmName = :farmName ORDER BY id DESC")
    fun getNotesByFarm(farmName: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE farmName = :farmName")
    suspend fun deleteAllNotesOfFarm(farmName: String)
}

@Dao
interface BackupDao {
    @Query("SELECT * FROM backups WHERE farmName = :farmName ORDER BY createdAt DESC")
    fun getBackupsByFarm(farmName: String): Flow<List<BackupEntity>>

    @Query("SELECT * FROM backups WHERE farmName = :farmName AND dateString = :dateString LIMIT 1")
    suspend fun getBackupByDate(farmName: String, dateString: String): BackupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackup(backup: BackupEntity)

    @Update
    suspend fun updateBackup(backup: BackupEntity)

    @Delete
    suspend fun deleteBackup(backup: BackupEntity)

    @Query("DELETE FROM backups WHERE farmName = :farmName")
    suspend fun deleteAllBackupsOfFarm(farmName: String)
}

@Dao
interface GoogleLinkDao {
    @Query("SELECT * FROM google_links ORDER BY linkedAt DESC")
    fun getAllLinks(): Flow<List<GoogleLinkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: GoogleLinkEntity)

    @Delete
    suspend fun deleteLink(link: GoogleLinkEntity)
    
    @Query("SELECT * FROM google_links WHERE googleEmail = :email")
    fun getLinksByEmail(email: String): Flow<List<GoogleLinkEntity>>
}

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
    fun getPendingSyncItems(): Flow<List<SyncQueueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncItem(item: SyncQueueEntity)

    @Delete
    suspend fun deleteSyncItem(item: SyncQueueEntity)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE farmName = :farmName ORDER BY reminderDateMillis ASC")
    fun getRemindersByFarm(farmName: String): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)
}

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines WHERE farmName = :farmName AND isArchived = 0 ORDER BY id DESC")
    fun getMedicinesByFarm(farmName: String): Flow<List<MedicineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity): Long

    @Update
    suspend fun updateMedicine(medicine: MedicineEntity)

    @Delete
    suspend fun deleteMedicine(medicine: MedicineEntity)

    @Query("DELETE FROM medicines WHERE farmName = :farmName")
    suspend fun deleteAllMedicinesOfFarm(farmName: String)
}

@Dao
interface BirthDao {
    @Query("SELECT * FROM births WHERE farmName = :farmName ORDER BY createdAt DESC")
    fun getBirthsByFarm(farmName: String): Flow<List<BirthEntity>>

    @Query("SELECT * FROM births WHERE motherId = :motherId ORDER BY createdAt DESC")
    fun getBirthsByMother(motherId: Int): Flow<List<BirthEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBirth(birth: BirthEntity): Long

    @Update
    suspend fun updateBirth(birth: BirthEntity)

    @Delete
    suspend fun deleteBirth(birth: BirthEntity)

    @Query("DELETE FROM births WHERE farmName = :farmName")
    suspend fun deleteAllBirthsOfFarm(farmName: String)
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs WHERE farmName = :farmName ORDER BY timestamp DESC")
    fun getLogsByFarm(farmName: String): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogEntity)

    @Delete
    suspend fun deleteLog(log: ActivityLogEntity)

    @Query("DELETE FROM activity_logs WHERE farmName = :farmName")
    suspend fun deleteAllLogsOfFarm(farmName: String)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records WHERE farmName = :farmName ORDER BY dateString ASC")
    suspend fun getAllAttendance(farmName: String): List<AttendanceEntity>

    @Query("SELECT * FROM attendance_records WHERE farmName = :farmName AND monthYear = :monthYear ORDER BY dateString ASC")
    fun getAttendanceByMonth(farmName: String, monthYear: String): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity)

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    @Delete
    suspend fun deleteAttendance(attendance: AttendanceEntity)

    @Query("DELETE FROM attendance_records WHERE farmName = :farmName")
    suspend fun deleteAllAttendanceOfFarm(farmName: String)
}

@Dao
interface PersonalAccountDao {
    @Query("SELECT * FROM personal_accounts WHERE farmName = :farmName ORDER BY name ASC")
    fun getPersonalAccountsByFarm(farmName: String): Flow<List<PersonalAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: PersonalAccountEntity): Long

    @Update
    suspend fun updateAccount(account: PersonalAccountEntity)

    @Delete
    suspend fun deleteAccount(account: PersonalAccountEntity)

    @Query("DELETE FROM personal_accounts WHERE farmName = :farmName")
    suspend fun deleteAllAccountsOfFarm(farmName: String)
}

@Dao
interface PersonalTransactionDao {
    @Query("SELECT * FROM personal_transactions WHERE farmName = :farmName ORDER BY createdAt DESC")
    fun getTransactionsByFarm(farmName: String): Flow<List<PersonalTransactionEntity>>

    @Query("SELECT * FROM personal_transactions WHERE accountId = :accountId ORDER BY createdAt DESC")
    fun getTransactionsByAccount(accountId: Int): Flow<List<PersonalTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: PersonalTransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: PersonalTransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: PersonalTransactionEntity)

    @Query("DELETE FROM personal_transactions WHERE accountId = :accountId")
    suspend fun deleteAllTransactionsOfAccount(accountId: Int)

    @Query("DELETE FROM personal_transactions WHERE farmName = :farmName")
    suspend fun deleteAllTransactionsOfFarm(farmName: String)
}

@Dao
interface AccountingDao {
    @Query("SELECT * FROM accounting_items WHERE farmName = :farmName ORDER BY timestamp DESC")
    fun getItemsByFarm(farmName: String): Flow<List<AccountingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: AccountingItem): Long

    @Update
    suspend fun updateItem(item: AccountingItem)

    @Delete
    suspend fun deleteItem(item: AccountingItem)

    @Query("SELECT * FROM accounting_items WHERE isSynced = 0")
    suspend fun getUnsyncedItems(): List<AccountingItem>
    
    @Query("UPDATE accounting_items SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markItemsSynced(ids: List<Int>)

    @Query("DELETE FROM accounting_items WHERE farmName = :farmName")
    suspend fun deleteAllItemsOfFarm(farmName: String)
}

@Database(
    entities = [
        FarmEntity::class,
        AnimalEntity::class,
        FeedEntity::class,
        PersonEntity::class,
        TransactionEntity::class,
        NoteEntity::class,
        BackupEntity::class,
        GoogleLinkEntity::class,
        SyncQueueEntity::class,
        RecycleBinEntity::class,
        ReminderEntity::class,
        MedicineEntity::class,
        BirthEntity::class,
        ActivityLogEntity::class,
        AttendanceEntity::class,
        PersonalAccountEntity::class,
        PersonalTransactionEntity::class,
        AccountingItem::class
    ],
    version = 13,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun farmDao(): FarmDao
    abstract fun animalDao(): AnimalDao
    abstract fun feedDao(): FeedDao
    abstract fun personDao(): PersonDao
    abstract fun transactionDao(): TransactionDao
    abstract fun noteDao(): NoteDao
    abstract fun backupDao(): BackupDao
    abstract fun googleLinkDao(): GoogleLinkDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun reminderDao(): ReminderDao
    abstract fun recycleBinDao(): RecycleBinDao
    abstract fun medicineDao(): MedicineDao
    abstract fun birthDao(): BirthDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun personalAccountDao(): PersonalAccountDao
    abstract fun personalTransactionDao(): PersonalTransactionDao
    abstract fun accountingDao(): AccountingDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `recycle_bin` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `farmName` TEXT NOT NULL, `itemType` TEXT NOT NULL, `originalId` INTEGER NOT NULL, `itemJson` TEXT NOT NULL, `deletedAt` INTEGER NOT NULL)"
                )
            }
        }
        
        val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `medicines` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `farmName` TEXT NOT NULL, `name` TEXT NOT NULL, `totalCost` REAL NOT NULL, `validityDays` INTEGER NOT NULL, `addedDate` TEXT NOT NULL, `isArchived` INTEGER NOT NULL DEFAULT 0)"
                )
            }
        }

        val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `births` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `farmName` TEXT NOT NULL, `motherId` INTEGER NOT NULL, `gender` TEXT NOT NULL, `birthType` TEXT NOT NULL, `birthDate` TEXT NOT NULL, `status` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)"
                )
            }
        }

        val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `notes` ADD COLUMN `title` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `notes` ADD COLUMN `colorHex` TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `people` ADD COLUMN `initialBalance` REAL NOT NULL DEFAULT 0.0")
            }
        }

        val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `births` ADD COLUMN `fatherId` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `medicines` ADD COLUMN `imageBase64` TEXT")
            }
        }

        val MIGRATION_10_11 = object : androidx.room.migration.Migration(10, 11) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `activity_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `farmName` TEXT NOT NULL, `actionType` TEXT NOT NULL, `entityType` TEXT NOT NULL, `description` TEXT NOT NULL, `dateString` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `attendance_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `farmName` TEXT NOT NULL, `dateString` TEXT NOT NULL, `monthYear` TEXT NOT NULL, `dayType` TEXT NOT NULL, `checkInTime` TEXT NOT NULL, `checkOutTime` TEXT NOT NULL, `isMorningShift` INTEGER NOT NULL, `note` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `personal_accounts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `farmName` TEXT NOT NULL, `name` TEXT NOT NULL, `phone` TEXT NOT NULL, `initialBalance` REAL NOT NULL, `balance` REAL NOT NULL, `note` TEXT NOT NULL DEFAULT '', `createdAt` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `personal_transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `farmName` TEXT NOT NULL, `accountId` INTEGER NOT NULL, `type` TEXT NOT NULL, `amount` REAL NOT NULL, `description` TEXT NOT NULL, `note` TEXT NOT NULL DEFAULT '', `dateString` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
            }
        }

        val MIGRATION_11_12 = object : androidx.room.migration.Migration(11, 12) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Ensure note columns exist in version 12
                try {
                    db.execSQL("ALTER TABLE `personal_accounts` ADD COLUMN `note` TEXT NOT NULL DEFAULT ''")
                } catch (e: Exception) { e.printStackTrace() }
                try {
                    db.execSQL("ALTER TABLE `personal_transactions` ADD COLUMN `note` TEXT NOT NULL DEFAULT ''")
                } catch (e: Exception) { e.printStackTrace() }
            }
        }

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "farm_pro_database"
                )
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
