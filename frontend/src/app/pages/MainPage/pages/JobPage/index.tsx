import {Button, Card, Space, Table, Tag, Tooltip} from 'antd';
import {useCallback, useEffect, useState} from 'react';
import styled from 'styled-components/macro';

import {
  BORDER_RADIUS,
  FONT_SIZE_TITLE,
  FONT_WEIGHT_MEDIUM,
  LINE_HEIGHT_TITLE,
  SPACE_LG,
  SPACE_MD
} from "styles/StyleConstants";
import {
  DeleteOutlined,
  EyeOutlined,
  FileTextOutlined,
  PlusOutlined,
  RedoOutlined,
  RetweetOutlined,
  StopOutlined
} from "@ant-design/icons";
import {useDispatch, useSelector} from "react-redux";
import {
  selectJobDeleteLoading,
  selectJobException,
  selectJobListLoading,
  selectJobLogsLoading,
  selectJobs,
  selectJobSaveLoading,
  selectJobUpdateLoading
} from "./slice/selectors";
import {Job} from "./slice/types"
import {CommonFormTypes} from "globalConstants";
import {JobForm} from "./JobForm";
import {JobFormModel} from "./types";
import {LogModal} from "./LogModal";
import {selectOrgId} from "../../slice/selectors";
import {ViewModal} from "./ViewModal";
import {DeleteModal} from "./DeleteModal";
import {useJobSlice} from "./slice";
import {changeJobState, exceptions, getJobs, jobDelete, saveJob} from "./slice/thunks";
import {RerunModal} from "./RerunModal";
import {CancelModal} from "./CancelModal";


export function JobPage() {

  useJobSlice()

  const [formType       , setFormType]        = useState(CommonFormTypes.Add);
  const [formVisible    , setFormVisible]     = useState(false);
  const [needRerunJob   , setNeedRerunJob]    = useState<undefined | Job>(void 0);
  const [rerunJobVisible, setRerunJobVisible] = useState(false);
  const [viewJob        , setViewJob]         = useState<undefined | Job> (void 0);
  const [viewVisible    , setViewVisible]     = useState(false);

  const dispatch = useDispatch();
  const orgId    = useSelector(selectOrgId);

  useEffect(() => {
    dispatch(getJobs()) // List jobs.
  }, [dispatch])

  const jobs          = useSelector(selectJobs);
  const jobExceptions = useSelector(selectJobException);
  const listLoading   = useSelector(selectJobListLoading);
  const saveLoading   = useSelector(selectJobSaveLoading);
  const deleteLoading = useSelector(selectJobDeleteLoading);
  const logsLoading   = useSelector(selectJobLogsLoading);
  const updateLoading = useSelector(selectJobUpdateLoading);

  const [job, setJob] = useState<undefined | Job>( void 0 );

  // Add.
  const showAddForm = useCallback(() => {
    setFormType(CommonFormTypes.Add);
    setFormVisible(true);
  }, []);

  // Refresh
  const doRefresh = useCallback(() => {
    dispatch(getJobs());
  }, [dispatch])

  // Rerun.
  const rerun = useCallback(
    id => () => {
      setNeedRerunJob(jobs.find(job => job.id === id));
      setRerunJobVisible(true);
    }
    , [jobs]
  );

  const doRerun = useCallback(
    () => {
      if (needRerunJob === undefined
            || needRerunJob.id === undefined) {
        return
      }

      dispatch(changeJobState({
        job: needRerunJob,
        state: 'RUNNING',
        resolve: () => {
          setRerunJobVisible(false); // hidden.
          dispatch(getJobs()); //
        }
      }))
    }, [needRerunJob]
  );

  const hideForm = useCallback(
    () => {
      setFormVisible(false);
      }
      , []
  );

  const afterFormClosed = useCallback(() => {
    // setEditingJob(void 0);
  }, []);

  // View.
  const showView = useCallback(
    id => () => {
      setViewJob(jobs.find(job => job.id === id));
      console.log(`viewed job< ${id}> and job = ${viewJob}`)
      setViewVisible(true )
    }
    , [jobs]
  );

  const hideView = useCallback(
    () => {
      setViewVisible(false);
    }, []
  )

  // Log.
  const [logVisible, setLogVisible] = useState(false)
  const showLog = useCallback(
    id => () => {
      // Fetch exception log.
      dispatch(exceptions(id))
      setLogVisible(true);
    }
    , [jobs]
  );
  const hideLog = useCallback(
    () => {
      setLogVisible(false);
    }, []
  )

  // Cancel
  const [cancelJob, setCancelJob] = useState<undefined | Job>(undefined)
  const [cancelModalVisible, setCancelModalVisible] = useState(false);
  const showCancelModal = useCallback(
    id => () => {
      setCancelJob(jobs.find(job => job.id === id));
      setCancelModalVisible(true);
    }, [jobs]
  )
  const hideCancelModal = useCallback(
    () => {
      setCancelJob(undefined)
      setCancelModalVisible(false);
    }, []
  )
  const doCancel = useCallback(
    () => {
      if (cancelJob === undefined
          || cancelJob.id === undefined) {
        return
      }

      dispatch(changeJobState({
        job: cancelJob,
        state: 'CANCELED',
        resolve: () => {
          setCancelModalVisible(false); // hidden.
          dispatch(getJobs()); // jobs.
        }
      }))
    }, [cancelJob]

  )

  // Delete.
  const [deleteJob, setDeleteJob] = useState<undefined | Job>(undefined);
  const [deleteFormVisible, setDeleteFormVisible] = useState(false);

  const showDeleteForm = useCallback(
    id => () => {
      setDeleteJob(jobs.find(job => job.id === id));
      setDeleteFormVisible(true);
    }, [jobs]
  )

  const hideDeleteForm = useCallback(
    () => {
      setDeleteFormVisible(false);
    }, []
  )

  const doDelete = useCallback(
    () => {

      if (deleteJob === undefined || deleteJob.id === undefined) {
        return
      }

      dispatch(jobDelete({
        job: deleteJob,
        resolve: () => {
          setDeleteFormVisible(false); // hidden.
          dispatch(getJobs()); // jobs.
        }
      })
      );
    }, [dispatch, deleteJob]
  )

  const save = useCallback(
    (values: JobFormModel) => {
      dispatch(
        saveJob({
          job: { ...values, orgId },
          resolve: () => {
            hideForm();
          }
        })
      );
    }
    , [dispatch, orgId]
  )

  const columns = [
    {
      title: '#',
      render: (_, record, index) => index + 1
    },
    {
      title: '作业ID',
      dataIndex: 'id',
      key: 'id',
      width: 120,
      render: _ => _.substring(_.length - 20, _.length)
    },
    {
      title: '作业名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '并行度',
      dataIndex: 'parallelism',
      key: 'parallelism',
      className: 'col-center',
    },
    {
      title: '状态',
      dataIndex: 'state',
      key: 'state',
      className: 'col-center',
      render: (text) => <Tag color = {text === 'RUNNING' ? 'green' : 'red'}>{text}</Tag>
    },
    {
      title: '操作人',
      dataIndex: 'createBy',
      key: 'updateBy',
      className: 'col-center',
    },
    {
      title: '更新时间',
      dataIndex: 'createTime',
      key: 'updateTime',
      className: 'col-center',
    },
    {
      title: '操作',
      width: 100,
      render: (_, record) => (
        <Actions>
          <Tooltip title='查看'>
            <Button
              type="link"
              icon={<EyeOutlined />}
              onClick={showView(record.id)}
            />
          </Tooltip>
          <Tooltip title='重新执行'>
            <Button
              type     = "link"
              icon     = { <RetweetOutlined /> }
              disabled = { record.state === 'RUNNING'}
              onClick  = { rerun(record.id) }
            />
          </Tooltip>
          <Tooltip title='日志'>
            <Button
              type="link"
              icon={<FileTextOutlined />}
              onClick = { showLog(record.id) }
            />
          </Tooltip>
          <Tooltip title='取消'>
            <Button
              type    = "link"
              icon    = {<StopOutlined />}
              onClick = { showCancelModal(record.id) }
              disabled= { ['UNKNOWN', 'CANCELED', 'CANCELING', 'FAILED'].includes(record.state) }
            />
          </Tooltip>
          <Tooltip title='删除'>
            <Button
              type    = "link"
              icon    = {<DeleteOutlined />}
              disabled= { record.state === 'RUNNING' }
              onClick = { showDeleteForm(record.id) }
            />
          </Tooltip>
        </Actions>
      )
    }
  ];
  return (
    <Wrapper>
      <Card>
        <TableHeader>
          <h3>实时任务列表</h3>
          <Toolbar>
            <Button
              icon={ <RedoOutlined /> }
              type="primary"
              onClick = { doRefresh }
            >
              刷新
            </Button>
            <Button
              icon={ <PlusOutlined /> }
              type="primary"
              onClick = { showAddForm }
            >
              新建
            </Button>
          </Toolbar>
        </TableHeader>
        <Table
          rowKey = "id"
          size = "small"
          dataSource = { jobs }
          columns    = { columns }
          loading    = { listLoading }
          // rowSelection={{ selectedRowKeys, onChange: setSelectedRowKeys }}
          // pagination={pagination}
        />
        <JobForm visible        = { formVisible }
                 orgId          = { orgId }
                 type           = { formType }
                 title          = '实时任务'
                 job            = { job }
                 onSave         = { save }
                 onCancel       = { hideForm }
                 afterClose     = { afterFormClosed }
                 closable       = { true }
                 maskClosable   = { false }
                 confirmLoading = { saveLoading }
        />
        <LogModal visible  = { logVisible }
                  logs     = { jobExceptions }
                  onCancel = { hideLog }
        />
        <ViewModal visible  = { viewVisible }
                   job      = { viewJob }
                   onCancel = { hideView }
        />
        <DeleteModal visible  = { deleteFormVisible }
                     job      = { deleteJob }
                     onCancel = { hideDeleteForm }
                     confirmLoading = { deleteLoading }
                     onOk     = { doDelete }
        />
        <RerunModal visible   = { rerunJobVisible }
                    job       = { needRerunJob }
                    onOk      = { doRerun }
                    onCancel  = { () => setRerunJobVisible(false) }
                    confirmLoading = { updateLoading }
        />
        <CancelModal visible  = { cancelModalVisible }
                     job      = { cancelJob }
                     onOk     = { doCancel }
                     onCancel = { hideCancelModal }
                     confirmLoading = { updateLoading }
        />
      </Card>
    </Wrapper>
  )
}

// ----------------------------------------------

// const Container = styled(Split)`
//   display: flex;
//   flex: 1;
//   min-width: 0;
//   min-height: 0;
// `;

const Wrapper = styled.div`
  flex: 1;
  padding: ${SPACE_LG};

  .ant-card {
    background-color: ${p => p.theme.componentBackground};
    border-radius: ${BORDER_RADIUS};
    box-shadow: ${p => p.theme.shadow1};

    .ant-card-body {
      padding: 0 ${SPACE_LG};
    }
  }
  .col-center {
    text-align: center;
  }

  tr th {
    text-align: center;
  }
`;

const TableHeader = styled.div`
  display: flex;
  align-items: center;
  padding: ${SPACE_MD} 0;

  h3 {
    flex: 1;
    font-size: ${FONT_SIZE_TITLE};
    font-weight: ${FONT_WEIGHT_MEDIUM};
    line-height: ${LINE_HEIGHT_TITLE};
  }


  th {
    text-align: center;
  }
`;

const Toolbar = styled(Space)`
  flex-shrink: 0;
`;

const Actions = styled(Space)`
  display: flex;
  justify-content: flex-end;
`;

