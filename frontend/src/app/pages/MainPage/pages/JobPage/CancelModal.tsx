import {memo} from "react";
import {Modal, ModalProps} from "antd";
import {ExclamationCircleOutlined} from "@ant-design/icons";
import {Job} from "./slice/types";

interface CancelModalProps extends ModalProps {
  visible: boolean;
  job: undefined | Job;
}
export const CancelModal = memo(
  ({
     visible
     , job
     , ...modalProps
   } : CancelModalProps) => {

    return (
      <Modal title = { <span><ExclamationCircleOutlined style={{ marginRight: 8 }}/>取消</span>}
             visible = { visible }
             {...modalProps}
      >
        是否确认取消&lt;{job?.name}&gt;?
      </Modal>
    )
  }
)
