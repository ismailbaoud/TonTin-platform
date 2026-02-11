import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

interface Transaction {
  id: number;
  darName: string;
  darInitials: string;
  darColor: string;
  date: string;
  type: string;
  amount: number;
  status: 'paid' | 'pending' | 'completed';
}

interface StatCard {
  title: string;
  value: string;
  icon: string;
  bgColor: string;
  borderColor: string;
  iconColor: string;
  badge: string;
  badgeBg: string;
  badgeText: string;
  subtext?: string;
}

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.scss'
})
export class ReportsComponent implements OnInit {
  activeTab: 'dar-circles' | 'personal-savings' = 'dar-circles';

  // Filter values
  dateRange = 'This Month';
  selectedDar = 'All Groups';
  selectedStatus = 'All Statuses';

  // Stats cards
  statCards: StatCard[] = [
    {
      title: 'Total Contributed',
      value: '$4,250.00',
      icon: 'account_balance_wallet',
      bgColor: 'bg-green-50',
      borderColor: 'border-green-100',
      iconColor: 'text-primary-dark',
      badge: '+12.5%',
      badgeBg: 'bg-green-100',
      badgeText: 'text-green-700'
    },
    {
      title: 'Pending Payments',
      value: '$350.00',
      icon: 'pending',
      bgColor: 'bg-yellow-50',
      borderColor: 'border-yellow-100',
      iconColor: 'text-yellow-600',
      badge: 'Due in 3 days',
      badgeBg: 'bg-yellow-100',
      badgeText: 'text-yellow-700'
    },
    {
      title: 'Expected Return',
      value: '$1,200.00',
      icon: 'payments',
      bgColor: 'bg-blue-50',
      borderColor: 'border-blue-100',
      iconColor: 'text-blue-600',
      badge: 'Next Payout',
      badgeBg: 'bg-blue-100',
      badgeText: 'text-blue-700',
      subtext: 'Oct 24, 2023'
    }
  ];

  // Chart data
  chartData = [
    { month: 'Jan', value: 300, height: 30 },
    { month: 'Feb', value: 450, height: 45 },
    { month: 'Mar', value: 400, height: 40 },
    { month: 'Apr', value: 600, height: 60 },
    { month: 'May', value: 550, height: 55 },
    { month: 'Jun', value: 800, height: 80 },
    { month: 'Jul', value: 700, height: 70 }
  ];

  // Payment status
  paymentStatus = [
    { label: 'On Time', percentage: 65, color: 'bg-primary' },
    { label: 'Early', percentage: 20, color: 'bg-gray-200' },
    { label: 'Late', percentage: 15, color: 'bg-amber-400' }
  ];

  // Transactions
  transactions: Transaction[] = [
    {
      id: 1,
      darName: 'Family Tontine',
      darInitials: 'FT',
      darColor: 'bg-green-100 text-green-800',
      date: 'Oct 12, 2023',
      type: 'Monthly Contribution',
      amount: 250.00,
      status: 'paid'
    },
    {
      id: 2,
      darName: 'Work Colleagues',
      darInitials: 'WC',
      darColor: 'bg-blue-100 text-blue-800',
      date: 'Oct 10, 2023',
      type: 'Penalty Fee',
      amount: 15.00,
      status: 'pending'
    },
    {
      id: 3,
      darName: 'Neighbors Circle',
      darInitials: 'NC',
      darColor: 'bg-purple-100 text-purple-800',
      date: 'Oct 01, 2023',
      type: 'Payout Received',
      amount: 1500.00,
      status: 'completed'
    },
    {
      id: 4,
      darName: 'Family Tontine',
      darInitials: 'FT',
      darColor: 'bg-green-100 text-green-800',
      date: 'Sep 12, 2023',
      type: 'Monthly Contribution',
      amount: 250.00,
      status: 'paid'
    }
  ];

  currentPage = 1;
  totalTransactions = 24;
  pageSize = 4;

  constructor() {}

  ngOnInit(): void {
    console.log('📊 Reports Component Initialized');
  }

  setActiveTab(tab: 'dar-circles' | 'personal-savings'): void {
    this.activeTab = tab;
  }

  applyFilters(): void {
    console.log('Applying filters:', {
      dateRange: this.dateRange,
      dar: this.selectedDar,
      status: this.selectedStatus
    });
    // TODO: Implement filter logic with backend
  }

  exportReport(): void {
    console.log('Exporting report...');
    // TODO: Implement export functionality
  }

  downloadCSV(): void {
    console.log('Downloading CSV...');
    // TODO: Implement CSV download
  }

  printReport(): void {
    console.log('Printing report...');
    window.print();
  }

  getStatusClass(status: string): string {
    const statusClasses: { [key: string]: string } = {
      'paid': 'bg-green-100 text-green-700',
      'pending': 'bg-amber-100 text-amber-700',
      'completed': 'bg-green-100 text-green-700'
    };
    return statusClasses[status] || 'bg-gray-100 text-gray-700';
  }

  getStatusLabel(status: string): string {
    const statusLabels: { [key: string]: string } = {
      'paid': 'Paid',
      'pending': 'Pending',
      'completed': 'Completed'
    };
    return statusLabels[status] || status;
  }

  formatAmount(amount: number, type: string): string {
    const formatted = amount.toFixed(2);
    return type === 'Payout Received' ? `+$${formatted}` : `$${formatted}`;
  }

  isPositiveAmount(type: string): boolean {
    return type === 'Payout Received';
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      // TODO: Load previous page data
    }
  }

  nextPage(): void {
    const totalPages = Math.ceil(this.totalTransactions / this.pageSize);
    if (this.currentPage < totalPages) {
      this.currentPage++;
      // TODO: Load next page data
    }
  }

  get paginationInfo(): string {
    const start = (this.currentPage - 1) * this.pageSize + 1;
    const end = Math.min(this.currentPage * this.pageSize, this.totalTransactions);
    return `Showing ${start}-${end} of ${this.totalTransactions} transactions`;
  }

  get canGoPrevious(): boolean {
    return this.currentPage > 1;
  }

  get canGoNext(): boolean {
    return this.currentPage < Math.ceil(this.totalTransactions / this.pageSize);
  }
}
