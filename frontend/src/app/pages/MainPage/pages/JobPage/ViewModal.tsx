import {memo, useEffect, useRef} from "react";
import {Button, Descriptions, Modal, ModalProps, Tag} from "antd";
import {Job} from "./slice/types";
import AceEditor from "react-ace";

interface ViewModalProps extends ModalProps {
  job: undefined | Job

}
export const ViewModal = memo(
  ({
    job
    , visible
    , onCancel
    , ...modalProps
   } : ViewModalProps) => {

    const editorRef = useRef<AceEditor | null>(null);

    useEffect(() => {
      // if (editorRef.current) {
      //     editorRef.current.editor?.setValue(`${job?.sql}`)
      //   }
    }, [])

    return (
      <Modal {...modalProps}
             visible = { visible }
             width = { 800 }
             footer = {
              [
                <Button key="close" onClick = { onCancel }>
                  关闭
                </Button>
              ] }
             onCancel = { onCancel }
             destroyOnClose
      >
        <Descriptions title="作业信息">
          <Descriptions.Item label="作业ID" >{job?.id.substr(job?.id.length - 20, job?.id.length)}</Descriptions.Item>
          <Descriptions.Item label="作业名称">{job?.name}</Descriptions.Item>
          <Descriptions.Item label="并行度">{job?.parallelism}</Descriptions.Item>
          <Descriptions.Item label="开始时间">{job?.startTime}</Descriptions.Item>
          <Descriptions.Item label="运行状态"><Tag color = {job?.state === 'RUNNING' ? 'green' : 'red'}>{job?.state}</Tag></Descriptions.Item>
          <Descriptions.Item label="创建人">{job?.createBy}</Descriptions.Item>
          <Descriptions.Item label="创建时间">{job?.createTime}</Descriptions.Item>
          <Descriptions.Item label="更新人">{job?.updateBy}</Descriptions.Item>
          <Descriptions.Item label="更新时间" span={3}>{job?.updateTime}</Descriptions.Item>
          <Descriptions.Item label="SQL脚本" span={3}>
            <AceEditor
              ref         = { editorRef }
              mode        = "sql"
              theme       = "github"
              name        = "my-editor"
              editorProps = {{ $blockScrolling: true }}
              width       = "100%"
              height      = "400px"
              readOnly    = {true}
              value       = {job?.sql}
            />
          </Descriptions.Item>
          <Descriptions.Item label="作业说明">{job?.description}</Descriptions.Item>
        </Descriptions>
      </Modal>
    )
});
