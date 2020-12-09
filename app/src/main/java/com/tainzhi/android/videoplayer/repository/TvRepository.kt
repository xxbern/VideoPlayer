package com.tainzhi.android.videoplayer.repository

import com.tainzhi.android.videoplayer.bean.Tv
import com.tainzhi.android.videoplayer.bean.TvProgram
import com.tainzhi.android.videoplayer.db.TvDao
import com.tainzhi.android.videoplayer.livedatanet.NetworkBoundRepository
import com.tainzhi.android.videoplayer.livedatanet.RateLimiter
import com.tainzhi.android.videoplayer.livedatanet.State
import com.tainzhi.mediaspider.TVSpider
import com.tainzhi.mediaspider.bean.TvProgramBean
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import java.util.concurrent.TimeUnit.MINUTES

/**
 * File:     TvRepository
 * Author:   tainzhi
 * Created:  2020/12/9 16:48
 * Mail:     QFQ61@qq.com
 * Description:
 */
class TvRepository(
        val tvDao: TvDao
) {

    // 1min更新一次
    val rateLimiter = RateLimiter<String>(1, timeUnit = MINUTES)

    companion object {
        const val TAG = "TvRepository.getTvPrograms"
    }

    fun loadTVs(): List<Tv> {
        val tvs = tvDao.getAllTv()
        tvs.forEach { tv ->
            tv.tvCircuit = loadTVSource(tv.id)
        }
        return tvs
    }

    /**
     * 查询卫视tvId的所有直播源
     *
     * @program tvId 卫视tvId
     */
    private fun loadTVSource(tvId: String): List<String> {
        return tvDao.getTvCircuit(tvId)
    }

    fun getTvPrograms(): Flow<State<Map<String, TvProgramBean>>> {
        return object : NetworkBoundRepository<Map<String, TvProgramBean>, Map<String, TvProgramBean>>() {
            override fun shouldFetch(): Boolean = rateLimiter.shouldFetch(TAG)

            override suspend fun saveRemoteData(response: Map<String, TvProgramBean>) {
                val tvProgramList = mutableListOf<TvProgram>()
                response.forEach { (_, u) ->
                    tvProgramList.add(TvProgram(u.tvId, u.liveProgram, u.liveProgramTime, u.nextProgram, u.nextProgramTime))
                }
                tvDao.insertAllTvPrograms(tvProgramList)
            }

            override fun fetchFromLocal(): Map<String, TvProgramBean> {
                val tvProgramBeanMap = hashMapOf<String, TvProgramBean>()
                val programs: List<TvProgram> = tvDao.getAllTvPrograms()
                programs.forEach { program ->
                    tvProgramBeanMap[program.tvId] =
                            TvProgramBean(program.tvId, program.liveProgram, program.liveProgramTime, program.nextProgram, program.nextProgramTime)
                }
                return tvProgramBeanMap

            }

            override suspend fun fetchFromRemote(): Response<Map<String, TvProgramBean>> {
                return coroutineScope {
                    val cctvJob = async {
                        TVSpider.getInstance().getTvProgram("cctv")
                    }
                    val stateJob = async {
                        TVSpider.getInstance().getTvProgram("satellite")
                    }
                    val response = Response.success(cctvJob.await() + stateJob.await())
                    response
                }
            }
        }.asFlow()
    }
}