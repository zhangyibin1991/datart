import {createAsyncThunk} from "@reduxjs/toolkit";
import {request2} from 'utils/request';
import {AddJobParams, Job, JobDeleteParam, JobStateParam, JobViewModel} from "./types";

export const getJobs =  createAsyncThunk<JobViewModel[], void>(
  'job/jobs',
  async orgId => {
    const { data } = await request2<JobViewModel[]>({
      url: '/jobs',
      method: 'GET'
    });

    return data;
  }
)

export const saveJob = createAsyncThunk<Job, AddJobParams>(
  'job/addJob',
  async ({job, resolve}) => {
  const { data } = await request2<Job>({
    url: '/jobs',
    method: 'POST',
    data: job ,
  });
  resolve();
  return data;
  }
)

export const changeJobState = createAsyncThunk<string, JobStateParam>(
  'job/rerunJob',
  async ({ job, state, resolve}) => {
    const { data } = await request2<string>({
      url: `/jobs/${job.id}/state`,
      method: "PUT",
      headers: {
        'Content-Type': 'application/json'
      },
      data: JSON.stringify(state)
    })

    resolve();

    return data
  }
)

// --------------------------------------------------------------//
//                                日志.                           //
// --------------------------------------------------------------//
export const exceptions = createAsyncThunk<string[], string>(
  'job/exceptions',
  async (job) => {
    const { data } = await request2<string[]>({
      url: `/jobs/${job}/exceptions`,
      method: 'GET',
    })

    return data;
  }
)

// --------------------------------------------------------------//
//                                删除.                           //
// --------------------------------------------------------------//
export const jobDelete = createAsyncThunk<String, JobDeleteParam>(
  'job/jobDelete',
  async ({ job, resolve }) => {
    const  { data } = await request2<string>({
      url: `/jobs/${job.id}`,
      method: 'DELETE',
      data: {}
    })

    // callback.
    resolve();

    return data;
  }
)
