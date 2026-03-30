package com.handylab.testapplication.data.repository

import com.handylab.testapplication.data.model.CherryBlossomInfo

interface KmaRepository {
    suspend fun getBlossomData(): List<CherryBlossomInfo>
}
