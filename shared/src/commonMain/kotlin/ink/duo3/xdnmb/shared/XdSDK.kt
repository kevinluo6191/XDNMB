package ink.duo3.xdnmb.shared

import ink.duo3.xdnmb.shared.data.cache.Database
import ink.duo3.xdnmb.shared.data.cache.DatabaseDriverFactory
import ink.duo3.xdnmb.shared.data.entity.Cookie
import ink.duo3.xdnmb.shared.data.entity.ForumGroup
import ink.duo3.xdnmb.shared.data.entity.Thread
import ink.duo3.xdnmb.shared.network.XdApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.periodUntil
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

class XdSDK(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = Database(databaseDriverFactory)
    private val api = XdApi()
    private val imageCDN = "https://image.nmb.best/"

    @Throws(Exception::class)
    suspend fun getForumList(forceReload: Boolean): List<ForumGroup> =
        withContext(Dispatchers.Default) {
            val cachedForumList = database.getAllForums()
            return@withContext if (cachedForumList.isNotEmpty() && !forceReload) {
                cachedForumList.also{
                    api.getForumList().also {
                        database.clearForumDatabase()
                        database.createForumList(it)
                    }
                }
            } else {
                api.getForumList().also {
                    database.clearForumDatabase()
                    database.createForumList(it)
                }
            }
        }

    @Throws(Exception::class)
    suspend fun getTimeLine(forceReload: Boolean, page: Int): List<Thread> =
        withContext(Dispatchers.Default) {
            var cachedThreadList = database.getAllTimeLine()
            if (cachedThreadList.isNotEmpty() && !forceReload) {
                val nextPage = api.getTimeLine(page)
                database.createTimeLine(nextPage)
                cachedThreadList = database.getAllTimeLine()
                return@withContext cachedThreadList
            } else {
                return@withContext api.getTimeLine(page).onEach { thread ->
                    thread.forumName = getForumName(thread.fid!!)
                }.also {
                    database.clearTimeLine()
                    database.createTimeLine(it)
                }
            }
        }

    @Throws(Exception::class)
    suspend fun getForumThreads(forumId: Int, forceReload: Boolean, page: Int): List<Thread> =
        withContext(Dispatchers.Default) {
            var cachedThreadList = database.getThreadsByForumId(forumId)
            val cookie = database.getSelectedCookie()
            return@withContext if (cachedThreadList.isNotEmpty() && !forceReload) {
                val nextPage = api.getTreadList(cookie?.cookie , forumId, page)
                database.createThreads(nextPage)
                cachedThreadList = database.getThreadsByForumId(forumId)
                return@withContext cachedThreadList
            } else {
                api.getTreadList(cookie?.cookie, forumId, page).also {
                    database.clearThreadsByForumId(forumId)
                    database.createThreads(it)
                }
            }
        }

    @Throws(Exception::class)
    suspend fun getReply(threadId: Int, page: Int): Thread =
        withContext(Dispatchers.Default) {
            val cookie = database.getSelectedCookie()
            return@withContext api.getReply(cookie?.cookie, threadId, page).also {
                database.createHistory(it)
            }
        }

    @Throws(Exception::class)
    suspend fun getHistory(): List<Thread> =
        withContext(Dispatchers.Default) {
            return@withContext database.getHistory()
        }

    @Throws(Exception::class)
    suspend fun getCookies(): List<Cookie>? =
        withContext(Dispatchers.Default) {
            return@withContext database.getAllCookie()
        }

    suspend fun addCookie(cookie: Cookie) {
        withContext(Dispatchers.Default) {
            var cachedCookies = database.getAllCookie()
            if (cachedCookies?.isEmpty() == true) {
                database.createCookie(cookie.copy(selected = true))
            } else {
                database.createCookie(cookie)
            }
        }
    }

    suspend fun deleteCookie(cookie: Cookie) {
        withContext(Dispatchers.Default) {
            database.clearCookieByName(cookie.cookie)
        }
    }

    @Throws(Exception::class)
    suspend fun getSelectedCookie(): Cookie? =
        withContext(Dispatchers.Default) {
            return@withContext database.getSelectedCookie()
        }


    fun imgToUrl(img: String, ext: String, isThumb: Boolean): String {
        var imageType = "image/"
        if (isThumb) {
            imageType = "thumb/"
        }
        return imageCDN + imageType + img + ext
    }

    fun getForumName(forumId: Int): String {
        return database.getForumName(forumId.toString())
    }

    fun formatTime(originalTime: String, inThread: Boolean): String {
        val timeZone = TimeZone.of("UTC+08:00")
        val originalTimeInISO = originalTime.replace(Regex("\\((.+?)\\)"), "T")
        val time = LocalDateTime.parse(originalTimeInISO)
        val currentInstant = Clock.System.now()
        val current = currentInstant.toLocalDateTime(timeZone)
        val timeInstant = time.toInstant(timeZone)

        val date = time.date.atTime(12, 0, 0, 0).toInstant(timeZone)
        val currentDate = currentInstant.toLocalDateTime(timeZone).date
            .atTime(12, 0, 0, 0).toInstant(timeZone)
        val diffInDay = date.periodUntil(currentDate, timeZone)
        val duration = currentInstant - timeInstant
        var result: String

        result = if (diffInDay.days < 1 && diffInDay.months == 0) {
            if (duration.inWholeHours < 1) {
                if (duration.inWholeMinutes < 1) {
                    duration.inWholeSeconds.toString() + "秒前"
                } else {
                    duration.inWholeMinutes.toString() + "分钟前"
                }
            } else {
                duration.inWholeHours.toString() + "小时前"
            }
        } else if (diffInDay.months == 0) {
            when (diffInDay.days) {
                -2 -> "后天"
                -1 -> "明天"
                1 -> "昨天"
                2 -> "前天"
                else -> time.monthNumber.toString() + "月" + time.dayOfMonth + "日"
            }
        } else {
            if (time.year == current.year) {
                time.monthNumber.toString() + "月" + time.dayOfMonth + "日"
            } else {
                time.year.toString() + "年" + time.monthNumber + "月" + time.dayOfMonth + "日"
            }
        }

        if (inThread) {
            if (diffInDay.days >= 1) {
                result =
                    result + " " + time.hour.let { if (it < 10) "0$it" else it } + ":" + time.minute.let { if (it < 10) "0$it" else it }
            } else if (duration.inWholeHours >= 1) {
                result = time.hour.let { if (it < 10) "0$it" else it.toString() } + ":" + time.minute.let { if (it < 10) "0$it" else it }
            }
        }

        return result
    }
}