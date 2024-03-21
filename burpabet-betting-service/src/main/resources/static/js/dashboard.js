var AppDashboard = function (settings) {
    this.settings = settings;
    this.init();
};

AppDashboard.prototype = {
    init: function () {
        this.betPlacedContainer = this.getElement(this.settings.elements.betPlacedContainer);
        this.betSettledContainer = this.getElement(this.settings.elements.betSettledContainer);
        this.raceSummaryContainer = this.getElement(this.settings.elements.raceSummaryContainer);

        this.loadInitialBets();
        this.loadInitialRaces();

        this.addWebsocketListener();
    },

    loadInitialBets: function () {
        var _this = this;

        $.get(this.settings.endpoints.settledBets, function (data) {
            var _e = data['_embedded'];
            if (_e && _e['betting:bet-list']) {
                var _container = _this.createSettledBetElements(_e['betting:bet-list']);
                _this.betSettledContainer.empty();
                _this.betSettledContainer.append(_container);
            }
        });

        $.get(this.settings.endpoints.unsettledBets, function (data) {
            var _e = data['_embedded'];
            if (_e && _e['betting:bet-list']) {
                var _container = _this.createUnsettledBetElements(_e['betting:bet-list']);
                _this.betPlacedContainer.empty();
                _this.betPlacedContainer.append(_container);
            }
        });
    },

    loadInitialRaces: function () {
        var _this = this;

        $.get(this.settings.endpoints.allRaces, function (data) {
            _this.createRaceElements(data['_embedded']['betting:race-list']);
        });
    },

    getElement: function (id) {
        return $('#' + id);
    },

    createSettledBetElements: function (data) {
        var _this = this, report;

        // console.log(data);

        report = data.map(function (bet) {
            var colorClass = '';

            if (bet.race.outcome === 'lose') {
                colorClass = 'table-danger';
            }

            return $('<tr>')
                // .attr('class', colorClass)
                .append(
                    $('<td>')
                        .attr('scope', 'row')
                        .append($('<a>')
                            .attr('href', bet._links.self.href)
                            .text(bet.customerName))
                )
                .append(
                    $('<td>')
                        // .attr('scope', 'row')
                        .text(bet.race.track)
                )
                .append(
                    $('<td>')
                        .attr('id', "horse")
                        .text(bet.race.horse)
                )
                .append(
                    $('<td>')
                        .attr('id', "odds")
                        .text(bet.race.odds)
                )
                .append(
                    $('<td>')
                        .attr('id', "outcome")
                        .text(bet.race.outcome)
                )
                .append(
                    $('<td>')
                        .attr('id', "stake")
                        .text(_this.formatMoney(bet.stake.amount, bet.stake.currency))
                )
                .append(
                    $('<td>')
                        .attr('id', "winnings")
                        .text(_this.formatMoney(bet.payout.amount, bet.payout.currency))
                )
        });

        return report;
    },

    createUnsettledBetElements: function (data) {
        var _this = this, report;

        // console.log(data);

        report = data.map(function (bet) {
            var colorClass = '';

            if (bet.race.outcome === 'lose') {
                colorClass = 'table-danger';
            }

            return $('<tr>')
                // .attr('class', colorClass)
                .append(
                    $('<td>')
                        .attr('scope', 'row')
                        .append($('<a>')
                            .attr('href', bet._links.self.href)
                            .text(bet.customerName))
                )
                .append(
                    $('<td>')
                        // .attr('scope', 'row')
                        .text(bet.race.track)
                )
                .append(
                    $('<td>')
                        .attr('id', "horse")
                        .text(bet.race.horse)
                )
                .append(
                    $('<td>')
                        .attr('id', "odds")
                        .text(bet.race.odds)
                )
                .append(
                    $('<td>')
                        .attr('id', "stake")
                        .text(_this.formatMoney(bet.stake.amount, bet.stake.currency))
                )
        });

        return report;
    },

    createRaceElements: function (data) {
        var _this = this, report;

        // console.log(data);

        report = data.map(function (race) {
            var colorClass = '';

            if (race.outcome === 'lose') {
                colorClass = 'table-danger';
            }
            return $('<tr>')
                // .attr('class', colorClass)
                .append(
                    $('<td>')
                        .attr('scope', 'row')
                        .append($('<a>')
                            .attr('href', race._links.self.href)
                            .text(race.track))
                )
                .append(
                    $('<td>')
                        .attr('id', "horse")
                        .text(race.horse)
                )
                .append(
                    $('<td>')
                        .attr('id', "odds")
                        .text(race.odds)
                )
                .append(
                    $('<td>')
                        .attr('id', "outcome")
                        .text(race.outcome)
                )
                .append(
                    $('<td>')
                        .attr('id', "totalBets")
                        .text(race.totalBets)
                )
                .append(
                    $('<td>')
                        .attr('id', "totalWager")
                        .text(_this.formatMoney(race.totalWager.amount, race.totalWager.currency))
                )
                .append(
                    $('<td>')
                        .attr('id', "totalPayout")
                        .text(_this.formatMoney(race.totalPayout.amount, race.totalPayout.currency))
                )
        });

        this.raceSummaryContainer.empty();
        this.raceSummaryContainer.append(report);
    },

    addWebsocketListener: function () {
        var socket = new SockJS(this.settings.endpoints.socket),
            stompClient = Stomp.over(socket),
            _this = this;

        stompClient.connect({}, function (frame) {
            stompClient.subscribe(_this.settings.topics.betSettlement, function (summary) {
                // var _event = JSON.parse(summary.body);
                // console.log("Stomp event (settlement): " + _event);
                _this.loadInitialBets();
            });

            stompClient.subscribe(_this.settings.topics.betPlacement, function (summary) {
                // var _event = JSON.parse(summary.body);
                // console.log("Stomp event (placement): " + _event);
                setTimeout(function(){
                    location.reload();
                }, 1000);
            });
        });
    },

    formatMoney: function (number, currency) {
        var formatter = new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: currency,
        });
        return formatter.format(number);
    }
};

document.addEventListener('DOMContentLoaded', function () {
    new AppDashboard({
        endpoints: {
            settledBets: '/api/bet/settled',
            unsettledBets: '/api/bet/unsettled',
            allRaces: '/api/race',
            socket: '/burpabet-betting'
        },

        topics: {
            betPlacement: '/topic/bet-placement',
            betSettlement: '/topic/bet-settlement'
        },

        elements: {
            betSettledContainer: 'bet-settled-container',
            betPlacedContainer: 'bet-placed-container',
            raceSummaryContainer: 'race-summary-container'
        }
    });
});

