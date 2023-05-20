import React, {memo} from 'react';
import {Button, Modal, ModalProps} from 'antd';

interface LogModalProps extends ModalProps {
  logs
}
export const LogModal = memo(
  ({
      visible
    , onCancel
    , logs
    , ... modalProps
   } : LogModalProps) => {
    return (
      <Modal
        {... modalProps}
        title     = "日志信息"
        width     = { 860 }
        bodyStyle = { { height: '500px', overflowX: 'scroll', overflowY: 'scroll' } }
        visible   = { visible }
        onCancel  = { onCancel }
        footer    = {
          [
            <Button key="close" onClick = { onCancel }>
              关闭
            </Button>
          ]
        }
        >
        {logs?.map((log, index) => (
          <p key={index}>{log}</p>
        ))}
      </Modal>
    );
  }
);
