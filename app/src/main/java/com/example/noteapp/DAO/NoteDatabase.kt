package com.example.noteapp.DAO
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.noteapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Database(entities = [Note::class], version = 4, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDAO

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example: database.execSQL("ALTER TABLE Note ADD COLUMN new_column INTEGER NOT NULL DEFAULT 0")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE notes ADD COLUMN creationTime INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                )   .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration() // Điều này sẽ xóa dữ liệu cũ khi cập nhật phiên bản database
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    populateDatabase(database.noteDao())
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateDatabase(noteDao: NoteDAO) {
            // Mỗi người dùng sẽ có các ghi chú mẫu riêng
            val userId = "sampleUserId" // Thay thế bằng userId thực tế nếu cần
            val currentTime = System.currentTimeMillis()
            // Thêm mẫu note cho người dùng
            val note1 = Note(
                title = "Ý tưởng kịch bản YouTube",
                description = "Có rất nhiều ứng dụng trên Android...",
                emoji = R.drawable.frame,
                imagePath = null,
                userId = userId,
                creationTime = currentTime
            )
            noteDao.insert(note1)

            val note2 = Note(
                title = "Ý tưởng blog về Datastore",
                description = "Datastore là một giải pháp lưu trữ dữ liệu mới...",
                emoji = R.drawable.frame,
                imagePath = null,
                userId = userId,
                creationTime = currentTime + 1000
            )
            noteDao.insert(note2)

            val note3 = Note(
                title = "Đánh giá tiểu phẩm trường đại học",
                description = "Tiểu phẩm gần đây của trường rất thú vị...",
                emoji = R.drawable.frame,
                imagePath = null,
                userId = userId,
                creationTime = currentTime + 2000
            )
            noteDao.insert(note3)

            val note4 = Note(
                title = "Ý tưởng blog mạng xã hội",
                description = "Mạng xã hội đang thay đổi cách chúng ta tương tác...",
                emoji = R.drawable.frame,
                imagePath = null,
                userId = userId,
                creationTime = currentTime + 3000
            )
            noteDao.insert(note4)
        }
    }
}
