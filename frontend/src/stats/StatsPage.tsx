import { useEffect, useState } from 'react'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend,
} from 'recharts'
import { fetchStats, type StatsResponse } from '../api/client'
import styles from './StatsPage.module.css'

const GENRE_COLORS = ['#2a7', '#3498db', '#e67e22', '#9b59b6', '#e74c3c']

export default function StatsPage() {
  const [stats, setStats] = useState<StatsResponse | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    fetchStats()
      .then(setStats)
      .catch(() => setError('統計情報の取得に失敗しました'))
  }, [])

  if (error) return <p className={styles.error}>{error}</p>
  if (!stats) return <p>読み込み中...</p>

  return (
    <div className={styles.page}>
      <h3>利用統計・業務レポート</h3>

      {/* サマリーカード */}
      <div className={styles.cards}>
        <StatCard label="総蔵書数"     value={stats.totalBooks}   unit="冊" />
        <StatCard label="登録利用者数" value={stats.totalMembers} unit="名" />
        <StatCard label="現在の貸出中" value={stats.activeLoans}  unit="冊" highlight />
        <StatCard label="延滞中"       value={stats.overdueLoans} unit="冊" warn={stats.overdueLoans > 0} />
      </div>

      <div className={styles.charts}>
        {/* 月別貸出数 */}
        <div className={styles.chartBox}>
          <h4>月別貸出数（直近6ヶ月）</h4>
          {stats.loansByMonth.length === 0 ? (
            <p className={styles.noData}>データがありません</p>
          ) : (
            <ResponsiveContainer width="100%" height={240}>
              <BarChart data={stats.loansByMonth} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="month" tick={{ fontSize: 12 }} />
                <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
                <Tooltip />
                <Bar dataKey="count" name="貸出数" fill="#2a7" radius={[3, 3, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* 人気ジャンル */}
        <div className={styles.chartBox}>
          <h4>人気ジャンル TOP5（分類番号別）</h4>
          {stats.topGenres.length === 0 ? (
            <p className={styles.noData}>データがありません</p>
          ) : (
            <ResponsiveContainer width="100%" height={240}>
              <PieChart>
                <Pie
                  data={stats.topGenres}
                  dataKey="count"
                  nameKey="genre"
                  cx="50%"
                  cy="50%"
                  outerRadius={90}
                  label={({ genre, percent }) =>
                    `${genre} ${(percent * 100).toFixed(0)}%`
                  }
                >
                  {stats.topGenres.map((_, i) => (
                    <Cell key={i} fill={GENRE_COLORS[i % GENRE_COLORS.length]} />
                  ))}
                </Pie>
                <Legend />
                <Tooltip formatter={(v) => [`${v}冊`, 'ジャンル']} />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>
    </div>
  )
}

function StatCard({
  label, value, unit, highlight, warn,
}: {
  label: string; value: number; unit: string; highlight?: boolean; warn?: boolean
}) {
  return (
    <div className={`${styles.card} ${highlight ? styles.cardHighlight : ''} ${warn ? styles.cardWarn : ''}`}>
      <p className={styles.cardLabel}>{label}</p>
      <p className={styles.cardValue}>{value.toLocaleString()}<span className={styles.cardUnit}>{unit}</span></p>
    </div>
  )
}
