import { LoaderCircle } from 'lucide-react'

export default function Spinner({ size = 18 }) {
  return <LoaderCircle size={size} className="animate-spin" aria-hidden="true" />
}
