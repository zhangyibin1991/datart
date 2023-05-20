import {memo} from "react";
import {Modal, ModalProps} from "antd";
import {ExclamationCircleOutlined} from "@ant-design/icons";
import {Job} from "./slice/types";

interface DeleteModalProps extends ModalProps {
  visible: boolean;
  job: undefined | Job;
}
export const DeleteModal = memo(
  ({
     visible
     , job
     , ...modalProps
    } : DeleteModalProps) => {

    return (
      <Modal title = { <span><ExclamationCircleOutlined style={{ marginRight: 8 }}/>删除</span>}
             visible = { visible }
             {...modalProps}
      >
        是否确认删除&lt;{job?.name}&gt;?
      </Modal>
    )
  }
)
