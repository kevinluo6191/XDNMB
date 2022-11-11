package ink.duo3.xdnmb.shared.data.cache

import ink.duo3.xdnmb.shared.data.entity.Forum
import ink.duo3.xdnmb.shared.data.entity.ForumGroup
import ink.duo3.xdnmb.shared.data.entity.Thread
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

internal class Database(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = AppDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = database.appDatabaseQueries

    internal fun clearForumDatabase() {
        dbQuery.transaction {
            dbQuery.removeAllForum()
            dbQuery.removeAllForumDetail()
        }
    }

    internal fun getAllForums(): List<ForumGroup> {
        val forumGroups = dbQuery.selectAllForumGroup(::mapForumGroupSelecting).executeAsList()
        return forumGroups
    }

    private fun mapForumGroupSelecting(
        id: String,
        name: String,
        sort: String,
        status: String
    ): ForumGroup {
        return ForumGroup(
            forums = dbQuery.selectForumByGroupId(id, ::mapForumSelecting).executeAsList(),
            id = id, name = name, sort = sort, status = status
        )
    }

    private fun mapForumSelecting(
        id: String,
        fgroup: String?,
        sort: String?,
        name: String,
        showName: String?,
        msg: String?,
        interval: String?,
        safeMode: String?,
        autoDelete: String?,
        threadCount: String?,
        permissionLevel: String?,
        forumFuseId: String?,
        createdAt: String?,
        updateAt: String?,
        status: String?
    ): Forum {
        return Forum(
            id, fgroup, sort, name, showName, msg, interval, safeMode, autoDelete, threadCount, permissionLevel, forumFuseId, createdAt, updateAt, status
        )
    }

    internal fun createForumList(forumList: List<ForumGroup>) {
        dbQuery.transaction {
            forumList.forEach {forumGroup ->
                insertForumGroup(forumGroup)

                forumGroup.forums.forEach {forum ->
                    insertForum(forum)
                }
            }
        }
    }

    private fun insertForumGroup(forumGroup: ForumGroup) {
        dbQuery.insertForumGroup(
            id = forumGroup.id,
            name = forumGroup.name,
            sort = forumGroup.sort,
            status = forumGroup.status,
        )
    }

    private fun insertForum(forum: Forum) {
        dbQuery.insertForum(
            forum.id,
            forum.fgroup,
            forum.sort,
            forum.name,
            forum.showName,
            forum.msg,
            forum.interval,
            forum.safeMode,
            forum.autoDelete,
            forum.threadCount,
            forum.permissionLevel,
            forum.forumFuseId,
            forum.createdAt,
            forum.updateAt,
            forum.status
        )
    }


    internal fun clearTimeLine() {
        dbQuery.transaction {
            dbQuery.removeAllTimeLineThread()
        }
    }

    internal fun getAllTimeLine(): List<Thread> {
        val threads = dbQuery.selectAllTimeLineThread(::mapThreadSelecting).executeAsList()
        return threads
    }

    private fun mapThreadSelecting(
        id: Int,
        fid: Int?,
        replyCount: Int?,
        img: String,
        ext: String,
        time: String,
        userHash: String,
        name: String,
        title: String,
        content: String,
        sage: Int?,
        admin: Int,
        hide: Int?,
        remainReplies:Int?,
        email: String?,
        master: Int?,
        page:Int
    ): Thread{
        return Thread(
            id, fid, replyCount, img, ext, time, userHash, name, title, content, sage, admin, hide, null, remainReplies, email, master, page
        )
    }

    internal fun createTimeLine(threadList: List<Thread>) {
        dbQuery.transaction {
            threadList.forEach {thread ->
                insertTimeLine(thread)
            }
        }
    }

    private fun insertTimeLine(thread: Thread) {
        dbQuery.insertTimeLineThread(
            thread.id,
            thread.fid,
            thread.replyCount,
            thread.img,
            thread.ext,
            thread.time,
            thread.userHash,
            thread.name,
            thread.title,
            thread.content,
            thread.sage,
            thread.admin,
            thread.hide,
            thread.remainReplies,
            thread.email,
            thread.master,
            thread.page
        )
    }

    internal fun getForumName(forumId: String): String {
        return dbQuery.selectForumNameByForumId(forumId).executeAsOne()
    }

    internal fun clearThreadsByForumId(forumId: Int) {
        dbQuery.transaction {
            dbQuery.removeThreadByForumId(forumId)
        }
    }

    internal fun getThreadsByForumId(forumId: Int): List<Thread> {
        val threads = dbQuery.selectThreadByForumId(forumId,::mapThreadSelecting).executeAsList()
        return threads
    }

    internal fun createThreads(threadList: List<Thread>) {
        dbQuery.transaction {
            threadList.forEach {thread ->
                insertThread(thread)
            }
        }
    }

    private fun insertThread(thread: Thread) {
        dbQuery.insertThread(
            thread.id,
            thread.fid,
            thread.replyCount,
            thread.img,
            thread.ext,
            thread.time,
            thread.userHash,
            thread.name,
            thread.title,
            thread.content,
            thread.sage,
            thread.admin,
            thread.hide,
            thread.remainReplies,
            thread.email,
            thread.master,
            thread.page
        )
    }

    internal fun clearAllHistory() {
        dbQuery.transaction {
            dbQuery.removeAllHistory()
        }
    }

    internal fun clearReplyByThreadIdAndPage(threadId: Int, page: Int) {
        dbQuery.transaction {
            dbQuery.removeReplyByThreadIdAndPage(threadId, page)
        }
    }

    internal fun getHistory(): List<Thread> {
        return dbQuery.selectAllHistory(::mapHistorySelecting).executeAsList()
    }

    private fun mapHistorySelecting(
        id: Int,
        fid: Int?,
        replyCount: Int?,
        img: String,
        ext: String,
        time: String,
        userHash: String,
        name: String,
        title: String,
        content: String,
        sage: Int?,
        admin: Int,
        hide: Int?,
        remainReplies:Int?,
        email: String?,
        master: Int?,
        page:Int,
        lastAccess: Long
    ): Thread{
        return Thread(
            id, fid, replyCount, img, ext, time, userHash, name, title, content, sage, admin, hide, null, remainReplies, email, master, page
        )
    }

    internal fun createHistory(thread: Thread) {
        dbQuery.transaction {
            insertHistory(thread)
        }
    }

    private fun insertHistory(thread: Thread) {
        dbQuery.insertHistory(
            thread.id,
            thread.fid,
            thread.replyCount,
            thread.img,
            thread.ext,
            thread.time,
            thread.userHash,
            thread.name,
            thread.title,
            thread.content,
            thread.sage,
            thread.admin,
            thread.hide,
            thread.remainReplies,
            thread.email,
            thread.master,
            thread.page,
            Clock.System.now().toEpochMilliseconds()
        )
    }

    private fun insertReply(threadId: Int, reply: Thread, page: Int){
        dbQuery.insertReply(
            threadId,
            reply.id,
            reply.fid,
            reply.replyCount,
            reply.img,
            reply.ext,
            reply.time,
            reply.userHash,
            reply.name,
            reply.title,
            reply.content,
            reply.sage,
            reply.admin,
            reply.hide,
            reply.remainReplies,
            reply.email,
            reply.master,
            page
        )
    }

}
