import request from '@/utils/request'

export interface UploadResult {
  url: string
  mediaType: 'image' | 'video'
}

const postMultipart = (path: string, file: File | Blob, filename?: string) => {
  const formData = new FormData()
  formData.append('file', file, filename)
  return request.post<unknown, { data: UploadResult }>(path, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export const uploadMedia = (file: File) => postMultipart('/files/upload', file, file.name)

export const uploadImage = (file: File | Blob, filename = 'image.jpg') =>
  postMultipart('/files/upload-image', file, filename)
